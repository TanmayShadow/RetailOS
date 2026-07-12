package com.example.RetailOS.platform.security.principal;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;
import com.example.RetailOS.platform.security.permission.Role;

public class UserPrincipal {
    UUID userId;
    UUID shopId;
    String email;
    Role role;
    Collection<GrantedAuthority> authorities;
}
