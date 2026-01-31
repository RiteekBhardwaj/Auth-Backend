package com.riteek.store.auth.services;

import com.riteek.store.auth.dtos.LoginResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;

public interface AuthService {

     long sendOtp(String entity, String email);
     void addUser(String email, String otp, String password);
     void setKey(String key, String value, Duration ttl);
     boolean isExpired(String key);
     void revoke(String key);
     UserDetails loadUserByUsername(String email);
     void changePassword(String email, String otp, String password);
     void otpVerification(String entity, String email, String otp);
}
