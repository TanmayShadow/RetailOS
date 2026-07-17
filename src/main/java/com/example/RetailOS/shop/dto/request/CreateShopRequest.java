package com.example.RetailOS.shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateShopRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;

    @Email
    @NotBlank
    @Size(max = 100)
    private String email;

    @Size(max = 15)
    private String gstIn;

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
}
