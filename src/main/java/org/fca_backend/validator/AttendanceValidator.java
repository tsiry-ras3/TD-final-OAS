package org.fca_backend.validator;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.fca_backend.DTO.CreateActivityMemberAttendanceDto;
import org.springframework.stereotype.Component;

@Component
public class AttendanceValidator {

    private static final String ERROR_MSG = "Either malformed provided data through request body or already confirmed attendance for any of provided member";

    public void validate(List<CreateActivityMemberAttendanceDto> dtos) throws BadRequestException {
        if (dtos == null || dtos.isEmpty()) {
            throw new BadRequestException(ERROR_MSG);
        }
        for (CreateActivityMemberAttendanceDto dto : dtos) {
            if (dto.getMemberIdentifier() == null || dto.getMemberIdentifier().isBlank()) {
                throw new BadRequestException(ERROR_MSG);
            }
            if (dto.getAttendanceStatus() == null) {
                throw new BadRequestException(ERROR_MSG);
            }
        }
    }
}