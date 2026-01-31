package com.riteek.store.auth.services;

import com.riteek.store.Redis.services.redisServiceImpl;
import com.riteek.store.auth.entitys.Auth;
import com.riteek.store.auth.repositories.AuthRepository;
import com.riteek.store.auth.types.Providers;
import com.riteek.store.auth.types.Roles;
import com.riteek.store.auth.utils.OtpUtil;
import com.riteek.store.email.service.EmailService;
import com.riteek.store.exceptions.CustomExceptions.BusinessException;
import com.riteek.store.exceptions.CustomExceptions.NotFoundException;
import com.riteek.store.exceptions.types.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final redisServiceImpl redisServiceImpl;
    private final AuthRepository authRepository;
    private final AuthTxnService authTxnService;

    @Override
    public long sendOtp(String entity, String email) {

        if (entity.equals("forgot")) {
            //account exists ?
            boolean isExist = authRepository.existsByEmail(email);
            if (!isExist) {
                throw new NotFoundException(ErrorCodes.NOT_FOUND, ErrorCodes.NOT_FOUND.getDefaultMessage());
            }
        }
        if (entity.equals("signup")) {
            //account exists ?
            boolean isExist = authRepository.existsByEmail(email);
            if (isExist) {
                throw new BusinessException(ErrorCodes.USER_EXIST, ErrorCodes.USER_EXIST.getDefaultMessage());
            }
        }

        //otp exists ?
        String otpKey = entity + ":otp:" + email;
        if (redisServiceImpl.keyExists(otpKey))
            throw new BusinessException(ErrorCodes.OTP_EXIST, ErrorCodes.OTP_EXIST.getDefaultMessage());

        //previous total otp sent in 24h
        long totalSentOtp = 0;
        String otpCountKey = entity + ":otp:count:" + email;
        if (redisServiceImpl.keyExists(otpCountKey)) {
            String raw = redisServiceImpl.getValue(otpCountKey);
            totalSentOtp = raw != null ? Long.parseLong(raw) : 0L;
            if (totalSentOtp > 10)
                throw new BusinessException(ErrorCodes.RATE_LIMIT, ErrorCodes.RATE_LIMIT.getDefaultMessage());
        }

        //previous failed credential or otp verification attempts in 24h
        long totalFailedAttempts = 0;
        String totalFailedAttemptsKey = "failed:attempts:count" + email;
        if (redisServiceImpl.keyExists(totalFailedAttemptsKey)) {
            String raw = redisServiceImpl.getValue("failed:attempts:count" + email);
            totalFailedAttempts = raw != null ? Long.parseLong(raw) : 0L;
            if (totalFailedAttempts > 20)
                throw new BusinessException(ErrorCodes.RATE_LIMIT, ErrorCodes.RATE_LIMIT.getDefaultMessage());
        }

        //generate and encode random 6 digit otp
        String otp = OtpUtil.generateOtp();
        String otpHash = passwordEncoder.encode(otp);
        Objects.requireNonNull(otpHash, "OTP hash cannot be null");

        //exponential backoff - if total sent otp > 3 double the ttl on each otp request
        long ttl = totalSentOtp < 3 ? 1 : (totalSentOtp - 2) * 2;

        //store otp hash in redis
        redisServiceImpl.setKey(otpKey, otpHash, Duration.ofMinutes(ttl));

        //send otp to email
        emailService.sendOtpEmail(email, otp);

        //increment otp count in redis
        redisServiceImpl.setNumberKey(otpCountKey);

        //set expiry of otp count only first time
        if (totalSentOtp == 0)
            redisServiceImpl.setExpiry(otpCountKey, Duration.ofDays(1));

        return ttl;
    }

    @Override
    public void addUser(String email, String otp, String password) {

        otpVerification("signup", email, otp);

        //encode password and save data in persistent db
        authTxnService.createAccount(email, password, Roles.AUTH_USER, Providers.LOGIN_LOCAL);

    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + email)
                );

        return new org.springframework.security.core.userdetails.User(
                auth.getEmail(),
                auth.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + auth.getRole().name()))
        );
    }

    @Override
    public void changePassword(String email, String otp, String password) {

        otpVerification("forgot", email, otp);

        authTxnService.changePassword(email, password);
    }

    @Override
    public void otpVerification(String entity, String email, String otp) {

        if (entity.equals("forgot")) {
            //account exists ?
            boolean isExist = authRepository.existsByEmail(email);
            if (!isExist) {
                throw new NotFoundException(ErrorCodes.NOT_FOUND, ErrorCodes.NOT_FOUND.getDefaultMessage());
            }
        }
        if (entity.equals("signup")) {
            //account exists ?
            boolean isExist = authRepository.existsByEmail(email);
            if (isExist) {
                throw new BusinessException(ErrorCodes.USER_EXIST, ErrorCodes.USER_EXIST.getDefaultMessage());
            }
        }

        //previous failed credential or otp verification attempts in 24h
        long totalFailedAttempts = 0;
        String totalFailedAttemptsKey = "failed:attempts:count" + email;
        if (redisServiceImpl.keyExists(totalFailedAttemptsKey)) {
            String raw = redisServiceImpl.getValue("failed:attempts:count" + email);
            totalFailedAttempts = raw != null ? Long.parseLong(raw) : 0L;
            if (totalFailedAttempts > 20)
                throw new BusinessException(ErrorCodes.RATE_LIMIT, ErrorCodes.RATE_LIMIT.getDefaultMessage());
        }

        //fetch otp hash from redis
        String otpKey = entity + ":otp:" + email;
        String otpHash = redisServiceImpl.getValue(otpKey);

        //otp expiry
        if (otpHash == null)
            throw new BusinessException(ErrorCodes.OTP_EXPIRED, ErrorCodes.OTP_EXPIRED.getDefaultMessage());

        //verify otp
        if (!passwordEncoder.matches(otp, otpHash)) {

            //increment failed attempts count in redis
            redisServiceImpl.setNumberKey(totalFailedAttemptsKey);

            //set expiry of failed attempts otp count only first time
            if (totalFailedAttempts == 0)
                redisServiceImpl.setExpiry(totalFailedAttemptsKey, Duration.ofDays(1));

            throw new BusinessException(ErrorCodes.OTP_INVALID, ErrorCodes.OTP_INVALID.getDefaultMessage());
        }

        //reset keys //entity
        redisServiceImpl.deleteKeys(totalFailedAttemptsKey, otpKey, (entity + ":otp:count:" + email));
    }

    @Override
    public void setKey(String key, String value, Duration ttl) {
        redisServiceImpl.setKey(key, value, ttl);
    }

    @Override
    public boolean isExpired(String key) {
        return redisServiceImpl.keyExists(key);
    }

    @Override
    public void revoke(String key) {
        redisServiceImpl.deleteKeys(key);
    }

}
