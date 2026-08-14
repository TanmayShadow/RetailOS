package com.example.RetailOS.shop.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Partial update — only non-null fields are applied.
 */
@Getter
@Setter
public class UpdateShopRequest {

    @Size(min = 3, max = 100)
    private String name;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;

    @Size(max = 15)
    private String gstIn;

    @Size(max = 255)
    private String address;

    @Size(max = 2048)
    private String logoUrl;

    @Size(max = 20)
    private String invoicePrefix;

    @Size(max = 1000)
    private String invoiceFooter;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid WhatsApp number")
    private String whatsappNumber;

    @Size(max = 100)
    private String upiId;

    /**
     * Optional — only applied when explicitly provided, so PATCHes that
     * don't touch branding don't accidentally flip it.
     */
    private Boolean showWatermark;
}
