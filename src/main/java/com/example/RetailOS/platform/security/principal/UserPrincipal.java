package com.example.RetailOS.platform.security.principal;

import com.example.RetailOS.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import com.example.RetailOS.platform.security.permission.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    String firstName;
    String lastName;
    UUID userId;
    UUID shopId;
    String email;
    private String password;
    Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authoritiesSet = new HashSet<>();
        authoritiesSet.add(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
        role.getPermissions()
             .forEach(permission ->
                     authoritiesSet.add(
                             new SimpleGrantedAuthority(permission.name())
                     )
             );
        return authoritiesSet;
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
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }

}
