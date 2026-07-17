package com.example.RetailOS.shop.entity;

import com.example.RetailOS.platform.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shops")
public class Shop extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    @Column(nullable = false, length = 15)
    private String phone;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(length = 15)
    private String gstIn;
    @Column(length = 2048, name="logo_url")
    private String logoUrl;
    @Column(name = "invoice_prefix", nullable = false, length = 20)
    @Builder.Default
    private String invoicePrefix = "INV";
    @Column(name = "invoice_footer", length = 1000)
    private String invoiceFooter;
    @Column(name = "whatsapp_number", length = 15)
    private String whatsappNumber;
    @Column(name = "upi_id", length = 100)
    private String upiId;
}
