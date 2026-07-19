package com.example.RetailOS.supplier.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "suppliers",
        indexes = @Index(name = "idx_suppliers_shop", columnList = "shop_id")
)
public class Supplier extends BaseEntity {

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 15)
    private String phone;
}
