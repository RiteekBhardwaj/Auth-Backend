package com.riteek.store.exceptions.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodes {

    //business exception codes
    INVALID_INPUT("Please enter valid input"),
    OTP_INVALID("Please enter correct otp"),
    CREDENTIALS_INVALID("Please enter correct credentials"),
    NOT_FOUND("User not found"),
    USER_EXIST("Account is already exist"),
    OTP_EXIST("Otp is already sent, please wait before sending new otp"),
    RATE_LIMIT("Too many requests, try again after 24 hours"),
    OTP_EXPIRED("OTP is expired, please send the otp again"),
    REFRESH_TOKEN_INVALID("Refresh token invalid, please login again to continue"),
    SESSION_EXPIRED("Session expired, please login again to continue"),

    //External service exception codes
    EMAIL_REQUEST_FAILED("Email request rejected by provider"),

    //service unavailable exception codes
    CACHE_DB_SERVICE_UNAVAILABLE("Caching service unavailable, please try again"),
    EMAIL_SERVICE_UNAVAILABLE("Email service unavailable, please try again"),
    PERSISTENT_DB_SERVICE_UNAVAILABLE("Persistent DB service unavailable, please try again"),

    //unknown exception codes
    UNKNOWN_ERROR("Something went wrong, Please try again");

    private final String defaultMessage;

}
