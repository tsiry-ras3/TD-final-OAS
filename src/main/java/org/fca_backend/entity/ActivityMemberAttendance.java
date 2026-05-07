package org.fca_backend.entity;

import org.fca_backend.DTO.MemberDescriptionDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ActivityMemberAttendance {
    private String id;
    private MemberDescriptionDto memberDescription;
    private AttendanceStatusEnum attendanceStatus;
}
