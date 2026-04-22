package org.fca_backend.controler;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.entity.Collectivity;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.service.CollectivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class CollectivityControler {
    CollectivityService collectivityService;

    @PostMapping("/collectivities")
    public ResponseEntity<?> createCollectivities(@RequestBody List<CreateCollectivityDTO> createCollectivityDTO){
        try{
            List<Collectivity> collectivities = collectivityService.createCollectivity(createCollectivityDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(collectivities);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
