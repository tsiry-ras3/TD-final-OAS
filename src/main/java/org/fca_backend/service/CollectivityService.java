package org.fca_backend.service;


import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.entity.Collectivity;
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
}
