package com.gatomalvado.web.controller;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.gatomalvado.common.MapperUtils;
import com.gatomalvado.consistent.contracts.Member;
import com.gatomalvado.consistent.core.Consistent;
import com.gatomalvado.web.dto.MemberResponse;
import com.gatomalvado.web.dto.request.ConsistentCreateDTO;
import com.gatomalvado.web.dto.request.LocationRequestDTO;
import com.gatomalvado.web.dto.response.ConsistentDTO;
import com.gatomalvado.web.dto.response.KeyMemberDTO;

@RestController
public class Controller {

//    private Consistent consistent;

    @PostMapping("/")
    public ResponseEntity<ConsistentDTO> createConsistent(@RequestBody ConsistentCreateDTO configDTO) {
        Consistent consistent = new Consistent(configDTO.getConfigDetails(), configDTO.getMemberDetails());
        ConsistentDTO dto = MapperUtils.createConsistentFrom(consistent);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/")
    public ResponseEntity<ConsistentDTO> getConsistent() {
        Consistent consistent = Consistent.getInstance();
        ConsistentDTO dto = MapperUtils.createConsistentFrom(consistent);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/location")
    public ResponseEntity<List<Map<String, MemberResponse>>> getKeyLocation(@RequestBody LocationRequestDTO requestDTO){
        Consistent consistent = Consistent.getInstance();
        List<Map<String, Member>> members = requestDTO.getLocateKeys().stream().map((k) -> Map.of(k, consistent.locateKey(ByteBuffer.wrap(k.getBytes())))).collect(
            Collectors.toUnmodifiableList());
        return ResponseEntity.ok(MapperUtils.createKeyToMemberMapping(members));
    }
}
