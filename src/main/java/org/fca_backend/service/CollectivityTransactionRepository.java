package org.fca_backend.service;

import org.fca_backend.entity.Transaction;

import java.util.List;

public class CollectivityTransactionRepository {
    CollectivityTransactionRepository collectivityTransactionRepository;
    public List<Transaction> getTransaction(String id){
        return collectivityTransactionRepository.getTransaction(id);
    }
}
