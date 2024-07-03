package com.gatomalvado.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gatomalvado.common.MapperUtils;
import com.gatomalvado.consistent.core.Consistent;
import com.gatomalvado.web.dto.ConsistentCreateDTO;
import com.gatomalvado.web.dto.ConsistentDTO;

@RestController
public class Controller {

    @PostMapping("/")
    public ResponseEntity<ConsistentDTO> createConsistent(@RequestBody ConsistentCreateDTO configDTO) {
        Consistent consistent = Consistent.init(configDTO.getConfigDetails(), configDTO.getMemberDetails());
        ConsistentDTO dto = MapperUtils.createConsistentFrom(consistent);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/")
    public ResponseEntity<ConsistentDTO> getConsistent() {
        Consistent consistent = Consistent.getInstance();
        ConsistentDTO dto = MapperUtils.createConsistentFrom(consistent);
        return ResponseEntity.ok(dto);
    }

//    public ResponseEntity<ConsistentDTO> addMember() {
//        return null;
//    }
}
