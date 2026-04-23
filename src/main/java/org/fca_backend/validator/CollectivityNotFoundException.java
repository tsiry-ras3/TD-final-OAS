package org.fca_backend.validator;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CollectivityNotFoundException extends RuntimeException {
    public CollectivityNotFoundException(UUID collectivityId) {
        super("Collectivity not found with id : " + collectivityId);
    }

    public CollectivityNotFoundException(String message) {
        super(message);
    }
}
