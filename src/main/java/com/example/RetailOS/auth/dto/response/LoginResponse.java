package com.example.RetailOS.auth.dto.response;


import com.example.RetailOS.platform.security.permission.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private UUID userId;

    private UUID shopId;

    private String firstName;

    private String lastName;

    private Role role;
}