package org.fca_backend.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinancialAccountDTO {
    private String id;
    private String accountType;
    private Double amount;
    
    // Mobile Banking specific
    private String holderName;
    private String mobileBankingService;
    private String mobileNumber;
    
    // Bank specific
    private String bankName;
    private Integer bankCode;
    private Integer bankBranchCode;
    private Integer bankAccountNumber;
    private Integer bankAccountKey;
}
