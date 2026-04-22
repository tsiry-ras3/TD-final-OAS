package org.fca_backend.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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
    private UUID id;
    private UUID collectivityId;
    private String label;
    private LocalDate eligibleDate;
    private Frequency frequency;
    private BigDecimal amount;
    private ActivityStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
