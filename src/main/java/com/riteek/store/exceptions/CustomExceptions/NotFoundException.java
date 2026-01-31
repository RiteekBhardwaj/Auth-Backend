package com.riteek.store.exceptions.CustomExceptions;

import com.riteek.store.exceptions.types.ErrorCodes;

public class NotFoundException extends RuntimeException {
    private final ErrorCodes errorCode;

    public NotFoundException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
