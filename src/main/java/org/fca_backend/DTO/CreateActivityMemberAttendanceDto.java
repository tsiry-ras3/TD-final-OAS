package org.fca_backend.DTO;

import org.fca_backend.entity.AttendanceStatusEnum;

import lombok.Data;

@Data
public class CreateActivityMemberAttendanceDto {
    private String memberIdentifier;
    private AttendanceStatusEnum attendanceStatus;
}