package com.example.RetailOS.user.dto.request;

import com.example.RetailOS.platform.security.permission.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {

    @NotNull
    private Role role;
}
