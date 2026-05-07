package org.fca_backend.service;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.fca_backend.DTO.CreateActivityMemberAttendanceDto;
import org.fca_backend.entity.ActivityMemberAttendance;
import org.fca_backend.repository.AttendanceRepository;
import org.fca_backend.repository.CollectivityActivityRepository;
import org.fca_backend.repository.CollectivityRepository;
import org.fca_backend.validator.AttendanceValidator;
import org.fca_backend.validator.CollectivityNotFoundException;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AttendanceService {
    private AttendanceRepository attendanceRepository;
    private AttendanceValidator attendanceValidator;
    private CollectivityRepository collectivityRepository;
    private CollectivityActivityRepository activityRepository;

    public List<ActivityMemberAttendance> addAttendances(
            String collectivityId,
            String activityId,
            List<CreateActivityMemberAttendanceDto> dtos) throws BadRequestException {

        if (!collectivityRepository.existsById(collectivityId)) {
            throw new CollectivityNotFoundException("Collectivity not found: " + collectivityId);
        }
        if (!activityRepository.existsById(activityId)) {
            throw new CollectivityNotFoundException("Activity not found: " + activityId);
        }

        attendanceValidator.validate(dtos);
        return attendanceRepository.addAttendances(activityId, dtos);
    }

    public List<ActivityMemberAttendance> getAttendances(String collectivityId, String activityId) {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new CollectivityNotFoundException("Collectivity not found: " + collectivityId);
        }
        if (!activityRepository.existsById(activityId)) {
            throw new CollectivityNotFoundException("Activity not found: " + activityId);
        }
        return attendanceRepository.getAttendances(collectivityId, activityId);
    }
}
