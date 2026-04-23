package org.fca_backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fca_backend.entity.PaymentMode;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateMemberPaymentDTO {
    private Double amount;
    private String membershipFeeIdentifier;
    private String accountCreditedIdentifier;
    private PaymentMode paymentMode;
}