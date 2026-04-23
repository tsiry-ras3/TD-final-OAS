package org.fca_backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.fca_backend.DTO.MembershipFeeDTO;
import org.fca_backend.entity.MembershipFee;
import org.fca_backend.repository.CollectivityRepository;
import org.fca_backend.repository.MembershipFeeRepository;
import org.fca_backend.validator.CollectivityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MembershipFeeService {
    private MembershipFeeRepository feeRepository;
    private CollectivityRepository collectivityRepository;

    public List<MembershipFeeDTO> getFeeByCollectivityId (UUID collectivityId) {
        // validator
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new CollectivityNotFoundException(collectivityId);
        }

        List<MembershipFee> fees = feeRepository.findByCollectivityId(collectivityId);
        return fees.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

        private MembershipFeeDTO toResponse(MembershipFee fee) {
        return new MembershipFeeDTO(
                fee.getId(),
                fee.getEligibleFrom(),
                fee.getFrequency(),
                fee.getAmount(),
                fee.getLabel(),
                fee.getStatus()
        );
    }
}