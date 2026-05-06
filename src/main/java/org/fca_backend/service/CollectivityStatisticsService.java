package org.fca_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.fca_backend.DTO.CollectivityLocalStatistics;
import org.fca_backend.repository.CollectivityStatisticsRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CollectivityStatisticsService {
    private CollectivityStatisticsRepository collectivityStatisticsRepository;

    public List<CollectivityLocalStatistics> getCollectivityStatistics(String collectivityId, LocalDate from, LocalDate to) {
        return collectivityStatisticsRepository.getStatistics(collectivityId, from, to);
    }
}
