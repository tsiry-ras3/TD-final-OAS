package org.fca_backend.controller;

import lombok.AllArgsConstructor;

import org.fca_backend.DTO.CreateActivityMemberAttendanceDto;
import org.fca_backend.DTO.CreateCollectivityActivityDto;
import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.UpdateCollectivityDTO;
import org.fca_backend.entity.ActivityMemberAttendance;
import org.fca_backend.entity.Collectivity;
import org.fca_backend.entity.CollectivityActivity;
import org.fca_backend.entity.FinancialAccount;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.exception.CollectivityNotFoundException;
import org.fca_backend.service.AttendanceService;
import org.fca_backend.service.CollectivityActivityService;
import org.fca_backend.service.CollectivityService;
import org.fca_backend.service.CollectivityStatisticsService;
import org.fca_backend.service.CollectivityTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@RestController
public class CollectivityController {
    CollectivityService collectivityService;
    CollectivityStatisticsService collectivityStatisticsService;
    CollectivityActivityService collectivityActivityService;
    CollectivityTransactionService collectivityTransactionService;
    AttendanceService attendanceService;

    @PostMapping("/collectivities")
    public ResponseEntity<?> createCollectivities(@RequestBody List<CreateCollectivityDTO> createCollectivityDTO) {
        try {
            List<Collectivity> collectivities = collectivityService.createCollectivity(createCollectivityDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(collectivities);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/collectivities/{collectivityId}")
    public ResponseEntity<?> updateCollectivity(
            @PathVariable String collectivityId,
            @RequestBody UpdateCollectivityDTO updateCollectivityDTO) {
        try {
            Collectivity collectivity = collectivityService.updateCollectivity(collectivityId, updateCollectivityDTO);
            return ResponseEntity.status(HttpStatus.OK).body(collectivity);
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Collectivity not found");
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/collectivities/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(collectivityActivityService.getActivities(id));
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Collectivity not found");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/collectivities/{id}")
    public ResponseEntity<?> getCollectivityById(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(collectivityService.getCollectivityById(id));
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/collectivities/{id}/financialAccounts")
    public ResponseEntity<?> getFinancialAccounts(
            @PathVariable String id,
            @RequestParam(required = false) LocalDate at) {
        try {
            List<FinancialAccount> accounts = collectivityService.getFinancialAccounts(id, at);
            return ResponseEntity.status(HttpStatus.OK).body(accounts);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving financial accounts");
        }
    }

    @GetMapping("/collectivities/{id}/statistics")
    public ResponseEntity<?> getCollectivityStatistics(
            @PathVariable String id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        try {
            List<?> statistics = collectivityStatisticsService.getCollectivityStatistics(id, from, to);
            return ResponseEntity.status(HttpStatus.OK).body(statistics);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving statistics");
        }
    }

    @PostMapping("/collectivities/{id}/activities")
    public ResponseEntity<?> addActivities(
            @PathVariable String id,
            @RequestBody List<CreateCollectivityActivityDto> dtos) {
        try {
            List<CollectivityActivity> created = collectivityActivityService.addNewActivities(id, dtos);
            return ResponseEntity.status(HttpStatus.OK).body(created);
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding activities");
        }
    }

    @GetMapping("/collectivities/{id}/activities")
    public ResponseEntity<?> getActivities(@PathVariable String id) {
        try {
            List<CollectivityActivity> activities = collectivityActivityService.getActivities(id);
            return ResponseEntity.status(HttpStatus.OK).body(activities);
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving activities");
        }
    }

    @PostMapping("/collectivities/{id}/activities/{activityId}/attendance")
    public ResponseEntity<?> addAttendance(
            @PathVariable String id,
            @PathVariable String activityId,
            @RequestBody(required = false) List<CreateActivityMemberAttendanceDto> dtos) {
        try {
            List<ActivityMemberAttendance> created = attendanceService.addAttendances(id, activityId, dtos);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding attendance");
        }
    }

    @GetMapping("/collectivities/{id}/activities/{activityId}/attendance")
    public ResponseEntity<?> getAttendance(
            @PathVariable String id,
            @PathVariable String activityId) {
        try {
            List<ActivityMemberAttendance> attendances = attendanceService.getAttendances(id, activityId);
            return ResponseEntity.status(HttpStatus.OK).body(attendances);
        } catch (CollectivityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving attendance");
        }
    }
}
