package com.gatomalvado.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gatomalvado.common.MapperUtils;
import com.gatomalvado.consistent.config.ConsistentConfig;
import com.gatomalvado.consistent.contracts.Member;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsistentCreateDTO {

    private long partitionCount;

    private long replicationFactor;

    private Set<Members> members;

    public List<Member> getMemberDetails() {
        return this.members.stream().map((mb) -> MapperUtils.createMemberFrom(mb)).collect(Collectors.toUnmodifiableList());
    }

    public ConsistentConfig getConfigDetails() {
        return ConsistentConfig.builder()
            .load(1.25)
            .partitionCount(this.partitionCount)
            .replicationFactor(this.replicationFactor)
            .hasher(b -> {
                HashFunction hashFunction = Hashing.murmur3_128();
                return hashFunction.hashBytes(b.array()).asLong();
            }).build();
    }
}
