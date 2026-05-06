package org.fca_backend.service;

import org.fca_backend.entity.Transaction;
import org.fca_backend.repository.CollectivityRepository;
import org.fca_backend.repository.CollectivityTransactionRepository;
import org.fca_backend.validator.CollectivityNotFoundException;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Service
public class CollectivityTransactionService {
    CollectivityRepository collectivityRepository;
    CollectivityTransactionRepository collectivityTransactionRepository;

    public List<Transaction> getTransaction(String id) {
        if (!collectivityRepository.existsById(id)) {
            throw new CollectivityNotFoundException("Collectivity not found: " + id);
        }
        return collectivityTransactionRepository.getCollectivityTransaction(id);
    }
}
