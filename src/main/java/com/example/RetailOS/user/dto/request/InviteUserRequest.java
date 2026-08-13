package com.example.RetailOS.user.dto.request;

import com.example.RetailOS.platform.security.permission.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * There's no email/invite-link infrastructure in this codebase yet, so
 * "inviting" a teammate creates their account directly with a password
 * an owner sets and relays to them out of band — not a pending-invite
 * record. See UserService for the sole-owner / SUPER_ADMIN guards.
 */
@Getter
@Setter
public class InviteUserRequest {

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 8, max = 50)
    private String password;

    @NotNull
    private Role role;
}
