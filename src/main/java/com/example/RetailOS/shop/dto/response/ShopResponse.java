package com.example.RetailOS.shop.dto.response;

import com.example.RetailOS.shop.entity.Shop;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ShopResponse {

    private UUID id;
    private String name;
    private String slug;
    private String phone;
    private String email;
    private String gstIn;
    private String logoUrl;
    private String invoicePrefix;
    private String invoiceFooter;
    private String whatsappNumber;
    private String upiId;
    private Instant createdAt;
    private Instant updatedAt;

    public static ShopResponse from(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .slug(shop.getSlug())
                .phone(shop.getPhone())
                .email(shop.getEmail())
                .gstIn(shop.getGstIn())
                .logoUrl(shop.getLogoUrl())
                .invoicePrefix(shop.getInvoicePrefix())
                .invoiceFooter(shop.getInvoiceFooter())
                .whatsappNumber(shop.getWhatsappNumber())
                .upiId(shop.getUpiId())
                .createdAt(shop.getCreatedAt())
                .updatedAt(shop.getUpdatedAt())
                .build();
    }
}
