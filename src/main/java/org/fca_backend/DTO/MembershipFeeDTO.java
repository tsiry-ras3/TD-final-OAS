package org.fca_backend.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.fca_backend.entity.ActivityStatus;
import org.fca_backend.entity.Frequency;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class MembershipFeeDTO {
    private UUID id;
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private BigDecimal amount;
    private String label;
    private ActivityStatus status;
}