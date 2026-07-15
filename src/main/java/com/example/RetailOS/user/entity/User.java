package com.example.RetailOS.user.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import com.example.RetailOS.platform.security.permission.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_shop", columnList = "shop_id")
        }
)
public class User extends BaseEntity {
    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Version
    private Long version;
}
