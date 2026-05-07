package org.fca_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.fca_backend.DTO.CollectivityLocalStatistics;
import org.fca_backend.entity.CollectivityOverall;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.exception.CollectivityNotFoundException;
import org.fca_backend.repository.CollectivityRepository;
import org.fca_backend.repository.CollectivityStatisticsRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CollectivityStatisticsService {
    private CollectivityStatisticsRepository collectivityStatisticsRepository;
    private CollectivityRepository collectivityRepository;

    // get statistics for a specific collectivity within a date range
    public List<CollectivityLocalStatistics> getCollectivityStatistics(String collectivityId, LocalDate from,
            LocalDate to) throws BadRequestException {
        if (!collectivityRepository.existsById(collectivityId)) {
            throw new CollectivityNotFoundException("Collectivity not found");
        }
        if (from == null || to == null) {
            throw new BadRequestException("Mandatory query parameters not provided or malformed.");
        }

        return collectivityStatisticsRepository.getStatistics(collectivityId, from, to);
    }

    // get overall statistics for all collectivities
    public List<CollectivityOverall> getOverallStatistics(LocalDate from, LocalDate to) throws BadRequestException {
        if (from == null || to == null) {
            throw new BadRequestException("Mandatory query parameters not provided or malformed.");
        }
        return collectivityStatisticsRepository.getOverallStatistics(from, to);
    }

}
