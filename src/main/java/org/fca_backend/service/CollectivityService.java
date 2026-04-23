package org.fca_backend.service;


import lombok.AllArgsConstructor;

import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.UpdateCollectivityDTO;
import org.fca_backend.entity.Collectivity;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.repository.CollectivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CollectivityService {
    CollectivityRepository collectivityRepository;

    public List<Collectivity> createCollectivity(List<CreateCollectivityDTO> collectivities){
        return collectivityRepository.addNewListOfCollectivity(collectivities);
    }
    public Collectivity updateCollectivity(String collectivityId, UpdateCollectivityDTO updateCollectivityDTO) {
        try {
            return collectivityRepository.updateCollectivity(collectivityId, updateCollectivityDTO);
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
