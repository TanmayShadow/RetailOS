package com.example.RetailOS.platform.security.service;

import com.example.RetailOS.platform.security.permission.Role;
import com.example.RetailOS.platform.security.principal.UserPrincipal;

import java.util.UUID;

public interface CurrentUserService {

    UserPrincipal getCurrentUser();

    UUID getCurrentUserId();

    UUID getCurrentShopId();

    Role getCurrentRole();

    String getCurrentEmail();

    boolean isAuthenticated();

}