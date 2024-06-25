package com.gatomalvado.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private Set<Long> sortedSet;                // this will be renamed based upon the use and significance
    private long partitionCount;                // will add its significance in the comment
    private Map<String, Double> loads;          // will add its significance
    private Map<String, Member> members;        // will add its significance
    private Map<Integer, Member> partitions;    // will add its significance
    private Map<Integer, Member> ring;

    @Getter
    private static Consistent instance;

    private Consistent(ConsistentConfig config, List<Member> memberList) {
        if(config.getHasher() == null){
            throw new RuntimeException("Hasher object can't be passed as null");
        }

        if(config.getPartitionCount() == 0){
            config.setPartitionCount(Constants.DEFAULT_PARTITION_COUNT);
        }

        if(config.getReplicationFactor() == 0) {
            config.setReplicationFactor(Constants.DEFAULT_REPLICATION_FACTOR);
        }

        if(config.getLoad() == 0.0){
            config.setLoad(Constants.DEFAULT_LOAD);
        }

        this.config = config;
        this.members = new ConcurrentHashMap<>();
        this.partitions = new ConcurrentHashMap<>();
        this.ring = new ConcurrentHashMap<>();
    }

    public static synchronized void INIT(ConsistentConfig config, List<Member> memberList) {
        instance = new Consistent(config, memberList);
    }

    public List<Member> getMembers(){
        readLock.lock();
        try{
            // Create a thread-safe copy of member list.
            List<Member> threadSafeMemberListCopy = new ArrayList<>(members.size());
            for(Member member: members.values()){
                threadSafeMemberListCopy.add(member);
            }
            return threadSafeMemberListCopy;
        } finally {
            readLock.unlock();
        }
    }

    public Double getAverageLoad(){
        readLock.lock();
        try{
            if(members.size() == 0){
                return 0.0;
            }
            double averageLoad = (config.getPartitionCount()/members.size())* config.getLoad();
            return Math.ceil(averageLoad);
        } finally {
            readLock.unlock();
        }
    }


}
