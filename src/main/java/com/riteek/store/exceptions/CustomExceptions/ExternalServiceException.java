package com.riteek.store.exceptions.CustomExceptions;

import com.riteek.store.exceptions.types.ErrorCodes;
import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {
    private final ErrorCodes errorCode;

    public ExternalServiceException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
