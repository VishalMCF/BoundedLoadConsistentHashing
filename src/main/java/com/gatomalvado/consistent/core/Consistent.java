package com.gatomalvado.consistent.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gatomalvado.consistent.common.Constants;
import com.gatomalvado.consistent.config.ConsistentConfig;
import com.gatomalvado.consistent.contracts.Hasher;
import com.gatomalvado.consistent.contracts.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Consistent {

    private ReentrantReadWriteLock reentrantRWLock = new ReentrantReadWriteLock();
    private Lock readLock = reentrantRWLock.readLock();
    private Lock writeLock = reentrantRWLock.writeLock();
    private ConsistentConfig config;
    private Hasher hasher;
    private TreeSet<Long> sortedSet;                // this will be renamed based upon the use and significance
    private long partitionCount;                // will add its significance in the comment
    private Map<String, Long> loads;          // will add its significance
    private Map<String, Member> members;        // will add its significance
    private Map<Long, Member> partitions;    // will add its significance
    private Map<Long, Member> ring;

    @Getter
    private static Consistent instance;

    public Consistent(ConsistentConfig config, List<Member> memberList) {
        synchronized (writeLock){
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
            this.sortedSet = new TreeSet<>();
            this.loads = new ConcurrentHashMap<>();
            this.hasher = this.config.getHasher();

            // add the members one by one to the hashtring
            for (Member member : memberList) {
                addMember(member);
            }
            if (members != null && members.size() > 0) {
                distributePartitions();
            }
        }
    }

    // method:- distributePartitions

    /**
     * What does the below method does exactly?
     */
    private void distributePartitions() {
        Map<String, Long> loads = new ConcurrentHashMap<>();
        Map<Long, Member> partitions = new ConcurrentHashMap<>();

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (long i = 1; i <= config.getPartitionCount(); i++) {
            buffer.putLong(i);
            long partitionHash = hasher.convertByteToHash(buffer);
            Long index = sortedSet.ceiling(partitionHash);
            if (index == null) {
                index = sortedSet.first();
            }
            distributeWithLoad(i, index, loads, partitions);
            buffer.clear();
        }

        this.loads = loads;
        this.partitions = partitions;
    }

    /**
     * whatever index I have received for the partitionId that needs to be allocated to some member and the load of the member needs to updated
     *
     * @param partitionId
     * @param keyHash
     * @param loads
     * @param partitions
     */
    public void distributeWithLoad(long partitionId, Long keyHash, Map<String, Long> loads, Map<Long, Member> partitions) {
        // calculate the average load
        double avgLoad = getAverageLoad();

        // find the partition
        int count = 0;
        while (true) {
            count++;

            if (count > this.sortedSet.size()) {
                throw new RuntimeException("not enough room to distribute partitions");
            }

            Member member = this.ring.get(keyHash);
            if (member == null) {
                throw new RuntimeException("member not found while doing load distribution for index -> " + keyHash);
            }

            // check the load
            Long loadOnTheMember = loads.getOrDefault(member.convertToString(), 0L);

            // if load is within the limit then assign that partition to the member
            if (loadOnTheMember + 1L <= avgLoad) {
                partitions.put(partitionId, member);
                loads.put(member.convertToString(), loadOnTheMember + 1L);
                return;
            }

            // if the load is greater, then move on to find some other member
            keyHash = sortedSet.higher(keyHash);

            if (keyHash == null) {
                keyHash = sortedSet.first();
            }
        }
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
            this.ring.put(partitionHash, member);
            this.sortedSet.add(partitionHash);
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

    /**
     * @param partitionId
     * @return
     */
    public Member getPartitionOwner(long partitionId) {
        writeLock.lock();
        try {
            if (!this.partitions.containsKey(Long.valueOf(partitionId))) {
                throw new RuntimeException("partition not found while finding owner of the partitionId -> " + partitionId);
            }
            return this.partitions.get(Long.valueOf(partitionId));
        } finally {
            writeLock.unlock();
        }
    }

    public Member locateKey(ByteBuffer key) {
        long partId = findPartitonId(key);
        return getPartitionOwner(partId);
    }

    public long findPartitonId(ByteBuffer key) {
        long hashedKey = hasher.convertByteToHash(key);
        var val = ((hashedKey) % (config.getPartitionCount()));
        return (val < 0 ? -1 * val : val);
    }

    public void remove(String name){
        writeLock.lock();
        try{
            if(!members.containsKey(name)){
                return;
            }
            for(int i=0; i<config.getReplicationFactor(); i++){
                long hash = hasher.convertByteToHash(ByteBuffer.wrap((name+"_"+(i+1)).getBytes()));
                this.ring.remove(hash);
                this.sortedSet.remove(hash);
            }
            members.remove(name);
            distributePartitions();
        } finally {
            writeLock.unlock();
        }
    }
}
