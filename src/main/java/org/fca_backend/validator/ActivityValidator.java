package org.fca_backend.validator;

import org.fca_backend.DTO.CreateCollectivityActivityDto;
import org.fca_backend.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivityValidator {

    public void validateCreateActivities(List<CreateCollectivityActivityDto> dtos) throws BadRequestException {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException("Activity list is null or empty");
        }

        for (CreateCollectivityActivityDto dto : dtos) {

            if (dto.getLabel() == null || dto.getLabel().trim().isEmpty()) {
                throw new BadRequestException("Activity label is required");
            }

            if (dto.getActivityType() == null) {
                throw new BadRequestException("Activity type is required for: " + dto.getLabel());
            }

            if (dto.getRecurrenceRule() == null && dto.getExecutiveDate() == null) {
                throw new BadRequestException("Either recurrenceRule or executiveDate is required for: " + dto.getLabel());
            }

            if (dto.getRecurrenceRule() != null && dto.getExecutiveDate() != null) {
                throw new BadRequestException("Cannot provide both recurrenceRule and executiveDate for: " + dto.getLabel());
            }
        }
    }
}