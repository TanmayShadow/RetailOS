package com.example.RetailOS.auth.controller;

import com.example.RetailOS.auth.dto.request.LoginRequest;
import com.example.RetailOS.auth.dto.request.RefreshTokenRequest;
import com.example.RetailOS.auth.dto.request.RegisterRequest;
import com.example.RetailOS.auth.dto.response.LoginResponse;
import com.example.RetailOS.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.register(request);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authenticationService.login(request)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request);
    }

}