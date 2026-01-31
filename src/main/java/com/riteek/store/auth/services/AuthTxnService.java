package com.riteek.store.auth.services;

import com.riteek.store.auth.entitys.Auth;
import com.riteek.store.auth.repositories.AuthRepository;
import com.riteek.store.auth.types.Providers;
import com.riteek.store.auth.types.Roles;
import com.riteek.store.exceptions.CustomExceptions.BusinessException;
import com.riteek.store.exceptions.types.ErrorCodes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTxnService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAccount(String email, String rawPassword, Roles role, Providers provider) {

        String hashedPassword = passwordEncoder.encode(rawPassword);
        Auth auth = new Auth(email, hashedPassword, role, provider);
        authRepository.save(auth);

    }

    @Transactional
    public void changePassword(String email, String rawPassword) {

        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.NOT_FOUND,
                        ErrorCodes.NOT_FOUND.getDefaultMessage()
                ));

        String hashedPassword = passwordEncoder.encode(rawPassword);
        auth.changePassword(hashedPassword);

        authRepository.save(auth);
    }

}
