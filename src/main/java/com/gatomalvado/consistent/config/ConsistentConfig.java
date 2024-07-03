package com.gatomalvado.consistent.config;

import com.gatomalvado.consistent.contracts.Hasher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Builder
public class ConsistentConfig {

    @Setter
    private Hasher hasher;

    @Setter
    private long partitionCount;

    @Setter
    private long replicationFactor;

    @Setter
    private double load;
}
