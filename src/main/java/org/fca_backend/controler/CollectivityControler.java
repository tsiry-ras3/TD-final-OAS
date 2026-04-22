package org.fca_backend.controler;

import lombok.AllArgsConstructor;
import org.fca_backend.DTO.CreateCollectivityDTO;
import org.fca_backend.DTO.UpdateCollectivityDTO;
import org.fca_backend.entity.Collectivity;
import org.fca_backend.exception.BadRequestException;
import org.fca_backend.repository.CollectivityTransactionRepository;
import org.fca_backend.service.CollectivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class CollectivityControler {
    CollectivityService collectivityService;
    CollectivityTransactionRepository ctRepository;

    @PostMapping("/collectivities")
    public ResponseEntity<?> createCollectivities(@RequestBody List<CreateCollectivityDTO> createCollectivityDTO){
        try{
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
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/collectivites/{id}/transactions")
    public ResponseEntity<?> getTransactions(@PathVariable String id){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(ctRepository.getCollectivityTransaction(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
