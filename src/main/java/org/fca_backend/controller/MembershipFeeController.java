package org.fca_backend.controller;

import java.util.List;
import java.util.UUID;

import org.fca_backend.DTO.MembershipFeeDTO;
import org.fca_backend.entity.MembershipFee;
import org.fca_backend.service.MembershipFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/collectivities/{id}/membershipFees")
public class MembershipFeeController {
    @Autowired
    private MembershipFeeService feeService;

    @GetMapping
    public List<MembershipFeeDTO> getFees(@PathVariable("id") UUID collectivityId) {
        return feeService.getFeeByCollectivityId(collectivityId);
    }

    @PostMapping
    public ResponseEntity<List<MembershipFeeDTO>> createMembershipFees(
            @PathVariable("id") UUID collectivityId,
            @Validated @RequestBody List<MembershipFee> fees) {
        
        List<MembershipFeeDTO> createdFees = feeService.createFees(collectivityId, fees);
        return ResponseEntity.ok(createdFees);
    }
}
