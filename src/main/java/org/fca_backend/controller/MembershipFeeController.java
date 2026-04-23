package org.fca_backend.controller;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.MembershipFeeDTO;
import org.fca_backend.entity.MembershipFee;
import org.fca_backend.service.MembershipFeeService;
import org.fca_backend.validator.CollectivityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequestMapping("/collectivities/{id}/membershipFees")
@RestController
public class MembershipFeeController {
    private MembershipFeeService feeService;

    @GetMapping
    public ResponseEntity<?> getFees(@PathVariable("id") UUID collectivityId) {
        try {
            return ResponseEntity.ok(feeService.getFeeByCollectivityId(collectivityId));
        }catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createMembershipFees(
            @PathVariable("id") UUID collectivityId,
            @Validated @RequestBody List<MembershipFee> fees) {
        try {
            return ResponseEntity.ok(feeService.createFees(collectivityId, fees));
        }catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
