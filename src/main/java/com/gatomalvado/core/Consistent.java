package com.gatomalvado.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gatomalvado.common.Constants;
import com.gatomalvado.config.ConsistentConfig;
import com.gatomalvado.contracts.Hasher;
import com.gatomalvado.contracts.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Consistent {

    private ReentrantReadWriteLock reentrantRWLock = new ReentrantReadWriteLock();
    private Lock readLock = reentrantRWLock.readLock();
    private Lock writeLock = reentrantRWLock.readLock();
    private ConsistentConfig config;
    private Hasher hasher;
    private ConcurrentSkipListSet<Long> sortedSet;                // this will be renamed based upon the use and significance
    private long partitionCount;                // will add its significance in the comment
    private Map<String, Long> loads;          // will add its significance
    private Map<String, Member> members;        // will add its significance
    private Map<Long, Member> partitions;    // will add its significance
    private Map<Long, Member> ring;

    @Getter
    private static Consistent instance;

    private Consistent(ConsistentConfig config, List<Member> memberList) {
        if (config.getHasher() == null) {
            throw new RuntimeException("Hasher object can't be passed as null");
        }

        if (config.getPartitionCount() == 0) {
            config.setPartitionCount(Constants.DEFAULT_PARTITION_COUNT);
        }

        if (config.getReplicationFactor() == 0) {
            config.setReplicationFactor(Constants.DEFAULT_REPLICATION_FACTOR);
        }

        if (config.getLoad() == 0.0) {
            config.setLoad(Constants.DEFAULT_LOAD);
        }

        this.config = config;
        this.members = new ConcurrentHashMap<>();
        this.partitions = new ConcurrentHashMap<>();
        this.ring = new ConcurrentHashMap<>();
        this.sortedSet = new ConcurrentSkipListSet<>();
        this.loads = new ConcurrentHashMap<>();

        distributePartitions();
    }

    // method:- distributePartitions

    /**
     * What does the below method does exactly?
     */
    private void distributePartitions() {
        Map<String, Double> loads = new HashMap<>();
        Map<Integer, Member> partitions = new HashMap<>();

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (long i = 0; i < config.getPartitionCount(); i++) {
            buffer.putLong(i);
            long partitionHash = hasher.convertByteToHash(buffer);
            Long index = sortedSet.ceiling(partitionHash);
            if (index == null) {
                index = 0L;
            }
            distributeWithLoad(i, index, loads, partitions);
        }
    }

    /**
     * whatever index I have received for the partitionId that needs to be allocated to some member and the load of the member needs to updated
     *
     * @param index
     * @param partitionId
     */
    public void distributeWithLoad(long partitionId, long index) {
        // calculate the average load
        double avgLoad = getAverageLoad();

        // find the partition
        int count = 0;
        while (true) {
            count++;

            if (count > this.sortedSet.size()) {
                throw new RuntimeException("not enough room to distribute partitions");
            }

            Member member = this.partitions.get(partitionId);
            if (member == null) {
                throw new RuntimeException("member not found while doing load distribution for index -> " + index);
            }

            // check the load
            Long loadOnTheMember = this.loads.get(member.convertToString());

            // if load is within the limit then assign that partition to the member
            if (loadOnTheMember + 1L <= avgLoad) {
                this.partitions.put(partitionId, member);
                this.loads.put(member.convertToString(), loadOnTheMember + 1L);
                return;
            }

            // if the load is greater, then move on to find some other member
            index++;
            if (index >= this.sortedSet.size()) {
                index = 0;
            }
        }
    }

    /**
     * @param config
     * @param memberList
     */
    public static synchronized void INIT(ConsistentConfig config, List<Member> memberList) {
        instance = new Consistent(config, memberList);
    }

    /**
     * @return
     */
    public List<Member> getMembers() {
        readLock.lock();
        try {
            // Create a thread-safe copy of member list.
            List<Member> threadSafeMemberListCopy = new ArrayList<>(members.size());
            for (Member member : members.values()) {
                threadSafeMemberListCopy.add(member);
            }
            return threadSafeMemberListCopy;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Method:- averageLoad present in the consistent hash
     *
     * @return
     */
    public Double getAverageLoad() {
        readLock.lock();
        try {
            if (members.size() == 0) {
                return 0.0;
            }
            double averageLoad = (config.getPartitionCount() / members.size()) * config.getLoad();
            return Math.ceil(averageLoad);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @param member
     */
    private void addMember(Member member) {
        // take account the replication factor. A member must be alloted the places on the ring
        // as many times as the replication factor is configured
        for (int i = 0; i < config.getReplicationFactor(); i++) {
            String name = member.convertToString() + "_" + (i + 1);
            byte[] nameInBytes = name.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(nameInBytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            long partitionHash = hasher.convertByteToHash(buffer);
            ring.put(partitionHash, member);
            sortedSet.add(partitionHash);
        }
        members.put(member.convertToString(), member);
    }

    /**
     * @param member
     */
    public void add(Member member) {
        writeLock.lock();
        try {
            if (members.containsKey(member.convertToString())) {
                return;
            }
            addMember(member);
            distributePartitions();
        } finally {
            writeLock.unlock();
        }
    }

}
