package org.fca_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {
    private String id;
    private LocalDateTime creationDate;
    private double amount;
    private PaymentMode paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;

}
