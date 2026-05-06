package org.fca_backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
@EqualsAndHashCode
@ToString
public class MembershipFee {
    private String id;
    private String collectivityId;
    private String label;
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private BigDecimal amount;
    private ActivityStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
