package com.riteek.store.exceptions.dtos;

import com.riteek.store.exceptions.types.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ExceptionResponse {
    private LocalDateTime timestamp;
    private int statusCode;
    private String error;
    private ErrorCodes errorCode;
    private String message;
    private String path;
}
