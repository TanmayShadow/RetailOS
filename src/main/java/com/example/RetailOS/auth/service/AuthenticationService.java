package com.example.RetailOS.auth.service;

import com.example.RetailOS.auth.Repository.RefreshTokenRepository;
import com.example.RetailOS.auth.dto.request.LoginRequest;
import com.example.RetailOS.auth.dto.request.RefreshTokenRequest;
import com.example.RetailOS.auth.dto.request.RegisterRequest;
import com.example.RetailOS.auth.dto.response.LoginResponse;
import com.example.RetailOS.auth.entity.RefreshToken;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.jwt.JwtProperties;
import com.example.RetailOS.platform.security.jwt.JwtTokenProvider;
import com.example.RetailOS.platform.security.permission.Role;
import com.example.RetailOS.platform.security.principal.UserPrincipal;
import com.example.RetailOS.user.entity.User;
import com.example.RetailOS.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtProperties jwtProperties;

    @Transactional
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already registered.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.OWNER)
                .active(true)
                .shopId(UUID.randomUUID()) // temporary
                .build();

        userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(principal);

        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(
                        Instant.now().plusMillis(
                                jwtProperties.getRefreshTokenExpiration()
                        )
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .shopId(user.getShopId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
    @Transactional
    public LoginResponse refreshToken(
            RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new BadCredentialsException(
                    "Refresh token revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException(
                    "Refresh token expired");
        }

        User user = refreshToken.getUser();

        UserPrincipal principal =
                UserPrincipal.from(user);

        String accessToken =
                jwtTokenProvider.generateAccessToken(principal);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .userId(principal.getUserId())
                .shopId(principal.getShopId())
                .firstName(principal.getFirstName())
                .lastName(principal.getLastName())
                .role(principal.getRole())
                .build();
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token"));

        if (!refreshToken.isRevoked()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }
}