package com.riteek.store.auth.utils;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {

    public static ResponseCookie generateCookie(String tokenName, String token, String path, Duration maxAge) {
        return ResponseCookie.from(tokenName, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("none")
                .path(path)
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie clearCookie(String tokenName, String path) {
        return ResponseCookie.from(tokenName, "")
                .httpOnly(true)
                .secure(true)
                .path(path)
                .maxAge(0)
                .build();
    }
}
