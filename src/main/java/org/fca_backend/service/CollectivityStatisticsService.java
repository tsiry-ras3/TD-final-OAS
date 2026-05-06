package org.fca_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.fca_backend.DTO.CollectivityLocalStatistics;
import org.fca_backend.entity.CollectivityOverall;
import org.fca_backend.repository.CollectivityStatisticsRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CollectivityStatisticsService {
    private CollectivityStatisticsRepository collectivityStatisticsRepository;

    // get statistics for a specific collectivity within a date range
    public List<CollectivityLocalStatistics> getCollectivityStatistics(String collectivityId, LocalDate from, LocalDate to) {
        return collectivityStatisticsRepository.getStatistics(collectivityId, from, to);
    }

    // get overall statistics for all collectivities
     public List<CollectivityOverall> getOverallStatistics (LocalDate from, LocalDate to) {
        return collectivityStatisticsRepository.getOverallStatistics(from, to);
     }
}
