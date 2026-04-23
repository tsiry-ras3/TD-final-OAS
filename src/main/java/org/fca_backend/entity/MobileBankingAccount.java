package org.fca_backend.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MobileBankingAccount implements FinancialAccount {
    private String id;
    private Double amount;
    private String holderName;
    private MobileBankingService mobileBankingService;
    private String mobileNumber;
}