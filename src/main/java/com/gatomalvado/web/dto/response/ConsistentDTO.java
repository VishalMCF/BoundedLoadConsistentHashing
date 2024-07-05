package com.gatomalvado.web.dto.response;

import java.util.Map;

import com.gatomalvado.web.dto.MemberResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsistentDTO {
    private Map<Long, MemberResponse> partitionMap;
    private Map<Long, MemberResponse> ring;
}
