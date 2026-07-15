package com.example.RetailOS.platform.security.service;

import com.example.RetailOS.platform.exception.UnauthenticatedException;
import com.example.RetailOS.platform.security.permission.Role;
import com.example.RetailOS.platform.security.principal.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public UserPrincipal getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthenticatedException("User is not authenticated.");
        }

        return (UserPrincipal) authentication.getPrincipal();
    }

    @Override
    public UUID getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    @Override
    public UUID getCurrentShopId() {
        return getCurrentUser().getShopId();
    }

    @Override
    public Role getCurrentRole() {
        return getCurrentUser().getRole();
    }

    @Override
    public String getCurrentEmail() {
        return getCurrentUser().getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}