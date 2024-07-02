package com.gatomalvado.config;

import com.gatomalvado.contracts.Hasher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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
