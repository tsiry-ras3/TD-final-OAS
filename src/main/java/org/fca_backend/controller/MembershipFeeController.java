package org.fca_backend.controller;

import java.util.List;
import java.util.UUID;

import org.fca_backend.DTO.MembershipFeeDTO;
import org.fca_backend.service.MembershipFeeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/collectivities/{id}/membershipFees")
public class MembershipFeeController {
    private MembershipFeeService feeService;

    @GetMapping
    public List<MembershipFeeDTO> getFees(@PathVariable("id") UUID collectivityId) {
        return feeService.getFeeByCollectivityId(collectivityId);
    }
}
