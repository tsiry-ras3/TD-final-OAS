package org.fca_backend.service;

import java.util.List;

import org.fca_backend.DTO.CreateCollectivityActivityDto;
import org.fca_backend.entity.CollectivityActivity;
import org.fca_backend.exception.CollectivityNotFoundException;
import org.fca_backend.repository.CollectivityActivityRepository;
import org.fca_backend.repository.CollectivityRepository;
import org.fca_backend.validator.ActivityValidator;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class CollectivityActivityService {
    private CollectivityRepository collectivityRepository;
    private CollectivityActivityRepository collectivityActivitiesRepository;
    private ActivityValidator activityValidator;

    public List<CollectivityActivity> addNewActivities(String id, List<CreateCollectivityActivityDto> dtos) {
        if (!collectivityRepository.existsById(id)) {
            throw new CollectivityNotFoundException("Collectivity not found: " + id);
        }
        activityValidator.validateCreateActivities(dtos);
        return collectivityActivitiesRepository.addNewActivities(id, dtos);
    }

    public List<CollectivityActivity> getActivities(String id) {
        if (!collectivityRepository.existsById(id)) {
            throw new CollectivityNotFoundException("Collectivity not found: " + id);
        }
        return collectivityActivitiesRepository.getActivities(id);
    }

}
