package com.riteek.store.auth.utils;

import java.security.SecureRandom;

public class OtpUtil {
    private static final SecureRandom random = new SecureRandom();

    public static String generateOtp() {
        int otp = 100000 + random.nextInt(900000); // 6-digit
        return String.valueOf(otp);
    }
}
