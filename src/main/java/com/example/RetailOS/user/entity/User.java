package com.example.RetailOS.user.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import com.example.RetailOS.platform.security.permission.Role;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private UUID shopId;

    private boolean active;
}
