package com.gatomalvado.web.dto;

import java.util.Map;

import com.gatomalvado.consistent.contracts.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsistentDTO {
    private Map<Long, MemberResponse> partitionMap;
    private Map<Long, MemberResponse> ring;
}
