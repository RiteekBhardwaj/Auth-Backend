package com.riteek.store.exceptions.CustomExceptions;

import com.riteek.store.exceptions.types.ErrorCodes;
import lombok.Getter;

@Getter
public class SystemException extends RuntimeException {
    private final ErrorCodes errorCode;
    private final String clientMessage;

    public SystemException(ErrorCodes errorCode, String clientMessage, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.clientMessage = clientMessage;
    }
}
