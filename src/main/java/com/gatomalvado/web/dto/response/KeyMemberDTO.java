package com.gatomalvado.web.dto.response;

import com.gatomalvado.consistent.contracts.Member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KeyMemberDTO {
    private String Key;
    private Member member;
}
