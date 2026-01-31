package com.riteek.store.auth.controllers;

import com.riteek.store.auth.dtos.*;
import com.riteek.store.auth.security.JwtService;
import com.riteek.store.auth.services.AuthServiceImpl;
import com.riteek.store.auth.utils.CookieUtil;
import com.riteek.store.exceptions.CustomExceptions.BusinessException;
import com.riteek.store.exceptions.types.ErrorCodes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.riteek.store.auth.utils.CookieUtil.clearCookie;
import static com.riteek.store.auth.utils.CookieUtil.generateCookie;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    //permission schema pending

    private final AuthServiceImpl authServiceImpl;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody OtpRequest otpRequest) {

        long ttl = authServiceImpl.sendOtp("signup", otpRequest.getEmail());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent successfully",
                "expiresIn", ttl * 60
        ));
    }

    //on signups create only ROLE_USER accounts
    @PostMapping("/create")
    public ResponseEntity<LoginResponse> create(@Valid @RequestBody SignupRequest signUpRequest) {

        authServiceImpl.addUser(signUpRequest.getEmail(), signUpRequest.getOtp(), signUpRequest.getPassword());

        //create authentication
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        signUpRequest.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_AUTH_USER"))
                );

        //generate tokens access token 10m refresh token 7days
        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);

        //store tokens in redis
        authServiceImpl.setKey(jwtService.extractJti(refreshToken), jwtService.extractUsername(refreshToken), Duration.ofDays(7));

        //create cookies
        ResponseCookie accessCookie = generateCookie("accessToken", accessToken, "/", Duration.ofMinutes(10));
        ResponseCookie refreshCookie = generateCookie("refreshToken", refreshToken, "/", Duration.ofDays(7));

        //response
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(
                        signUpRequest.getEmail(),
                        List.of("ROLE_AUTH_USER")
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {

        //credential verification
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BusinessException(
                    ErrorCodes.CREDENTIALS_INVALID,
                    ErrorCodes.CREDENTIALS_INVALID.getDefaultMessage()
            );
        }

        UserDetails user = (UserDetails) auth.getPrincipal();

        //generate tokens access token 10m refresh token 7days
        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);

        //store tokens in redis
        authServiceImpl.setKey(jwtService.extractJti(refreshToken), jwtService.extractUsername(refreshToken), Duration.ofDays(7));

        //create cookies
        ResponseCookie accessCookie = CookieUtil.generateCookie("accessToken", accessToken, "/", Duration.ofMinutes(10));
        ResponseCookie refreshCookie = CookieUtil.generateCookie("refreshToken", refreshToken, "/", Duration.ofDays(7));

        //response
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(
                        user.getUsername(),
                        user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue("refreshToken") String refreshToken
    ) {

        //delete refresh token from redis
        if (refreshToken != null) {
            String jti = jwtService.extractJti(refreshToken);
            authServiceImpl.revoke(jti);
        }

        //clear cookies
        ResponseCookie accessCookie = clearCookie("accessToken", "/");
        ResponseCookie refreshCookie = clearCookie("refreshToken", "/");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue("refreshToken") String refreshToken
    ) {

        //token valid ?
        if (!jwtService.isValid(refreshToken)) {
            throw new BusinessException(ErrorCodes.REFRESH_TOKEN_INVALID, ErrorCodes.REFRESH_TOKEN_INVALID.getDefaultMessage());
        }

        String oldJti = jwtService.extractJti(refreshToken);
        String username = jwtService.extractUsername(refreshToken);

        //token expired ?
        if (!authServiceImpl.isExpired(oldJti)) {
            throw new BusinessException(ErrorCodes.SESSION_EXPIRED, ErrorCodes.SESSION_EXPIRED.getDefaultMessage());
        }

        //delete old refresh token from redis
        authServiceImpl.revoke(oldJti);

        //fetch user
        UserDetails user = authServiceImpl.loadUserByUsername(username);

        //create authentication
        Authentication auth =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());

        //generate tokens access token 10m refresh token 7days
        String newAccessToken = jwtService.generateAccessToken(auth);
        String newRefreshToken = jwtService.generateRefreshToken(auth);

        //store tokens in redis
        String newJti = jwtService.extractJti(newRefreshToken);
        authServiceImpl.setKey(newJti, username, Duration.ofDays(7));

        //create cookies
        ResponseCookie accessCookie = CookieUtil.generateCookie("accessToken", newAccessToken, "/", Duration.ofMinutes(10));
        ResponseCookie refreshCookie = CookieUtil.generateCookie("refreshToken", newRefreshToken, "/", Duration.ofDays(7));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new LoginResponse(
                        username,
                        user.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                ));
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@Valid @RequestBody OtpRequest otpRequest) {

        long ttl = authServiceImpl.sendOtp("forgot", otpRequest.getEmail());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent successfully",
                "expiresIn", ttl * 60
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody SignupRequest signUpRequest) {

        authServiceImpl.changePassword(signUpRequest.getEmail(), signUpRequest.getOtp(), signUpRequest.getPassword());

        //response
        return ResponseEntity.ok(Map.of(
                "message", "Password changed successfully"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(Authentication authentication) {

        UserDetails user =
                authServiceImpl.loadUserByUsername(authentication.getName());

        return ResponseEntity.ok(
                new LoginResponse(
                        user.getUsername(),
                        user.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
        );
    }
}
