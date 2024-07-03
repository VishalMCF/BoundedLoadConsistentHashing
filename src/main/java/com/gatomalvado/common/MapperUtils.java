package com.gatomalvado.common;


import java.util.HashMap;
import java.util.Map;

import com.gatomalvado.consistent.contracts.Member;
import com.gatomalvado.consistent.core.Consistent;
import com.gatomalvado.web.dto.ConsistentDTO;
import com.gatomalvado.web.dto.MemberResponse;
import com.gatomalvado.web.dto.Members;

public class MapperUtils {

    public static ConsistentDTO createConsistentFrom(Consistent consistent) {
        return ConsistentDTO.builder()
            .ring(convertPartitionsMap(consistent.getRing()))
            .partitionMap(convertPartitionsMap(consistent.getPartitions()))
            .build();
    }

    public static Member createMemberFrom(Members memberDTO) {
        return () -> memberDTO.getName();
    }

    public static MemberResponse createMemberResponse(Member member) {
        return new MemberResponse(member.convertToString());
    }

    public static Map<Long, MemberResponse> convertPartitionsMap(Map<Long, Member> memberMap) {
        Map<Long, MemberResponse> memberResponseMap = new HashMap<>();
        for (Map.Entry<Long, Member> entry : memberMap.entrySet()) {
            Long key = entry.getKey();
            Member member = entry.getValue();
            MemberResponse memberResponse = MapperUtils.createMemberResponse(member);
            memberResponseMap.put(key, memberResponse);
        }
        return memberResponseMap;
    }
}
