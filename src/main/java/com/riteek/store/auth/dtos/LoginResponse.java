package com.riteek.store.auth.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

    private String username;
    private List<String> roles;
}

