package com.riteek.store.exceptions.CustomExceptions;

import com.riteek.store.exceptions.types.ErrorCodes;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCodes errorCode;

    public BusinessException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
