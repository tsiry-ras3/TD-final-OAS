package org.fca_backend.DTO;

import java.time.LocalDate;
import java.util.List;

import org.fca_backend.entity.ActivityType;
import org.fca_backend.entity.MemberOccupation;
import org.fca_backend.entity.MonthlyRecurrenceRule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreateCollectivityActivityDto {
    private String label;
    private ActivityType activityType;
    private List<MemberOccupation> memberOccupationConcerned;
    private MonthlyRecurrenceRule recurrenceRule;
    private LocalDate executiveDate;

}
