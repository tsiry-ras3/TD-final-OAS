package org.fca_backend.validator;

import org.fca_backend.DTO.CreateCollectivityActivityDto;
import org.fca_backend.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActivityValidator {
    String errorMessage = "Both recurrence rule and executive date provided, or provided data malformed inside payload.";

    public void validateCreateActivities(List<CreateCollectivityActivityDto> dtos) throws BadRequestException {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException(errorMessage);
        }

        for (CreateCollectivityActivityDto dto : dtos) {

            if (dto.getLabel() == null || dto.getLabel().trim().isEmpty()) {
            throw new BadRequestException(errorMessage);
            }

            if (dto.getActivityType() == null) {
            throw new BadRequestException(errorMessage);
            }

            if (dto.getRecurrenceRule() == null && dto.getExecutiveDate() == null) {
            throw new BadRequestException(errorMessage);
            }

            if (dto.getRecurrenceRule() != null && dto.getExecutiveDate() != null) {
            throw new BadRequestException(errorMessage);
            }
        }
    }
}