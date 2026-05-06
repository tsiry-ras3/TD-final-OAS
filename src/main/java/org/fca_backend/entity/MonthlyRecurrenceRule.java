package org.fca_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MonthlyRecurrenceRule {
    private Integer weekOrdinal; 
    private String dayOfWeek;
}
