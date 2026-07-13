package com.example.RetailOS.platform.security.principal;

import com.example.RetailOS.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import com.example.RetailOS.platform.security.permission.Role;
import org.springframework.security.core.userdetails.UserDetails;


@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    UUID userId;
    UUID shopId;
    String email;
    private String password;
    Role role;
    Collection<GrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    public static UserPrincipal from(User user) {

        return UserPrincipal.builder()
                .userId(user.getId())
                .shopId(user.getShopId())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .authorities(Collections.emptyList()) // we'll replace later
                .build();
    }

}
