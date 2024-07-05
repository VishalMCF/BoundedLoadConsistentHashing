package com.gatomalvado.common;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gatomalvado.consistent.contracts.Member;
import com.gatomalvado.consistent.core.Consistent;
import com.gatomalvado.web.dto.response.ConsistentDTO;
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

    public static <T> Map<T, MemberResponse> convertPartitionsMap(Map<T, Member> memberMap) {
        Map<T, MemberResponse> memberResponseMap = new HashMap<>();
        for (Map.Entry<T, Member> entry : memberMap.entrySet()) {
            T key = entry.getKey();
            Member member = entry.getValue();
            MemberResponse memberResponse = MapperUtils.createMemberResponse(member);
            memberResponseMap.put(key, memberResponse);
        }
        return memberResponseMap;
    }

    public static List<Map<String, MemberResponse>> createKeyToMemberMapping(List<Map<String, Member>> members) {
        return members.stream().map(MapperUtils::convertPartitionsMap).collect(Collectors.toUnmodifiableList());
    }
}
