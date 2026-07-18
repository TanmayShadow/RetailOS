package com.example.RetailOS.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateShopSubscriptionRequest {

    @NotNull
    private UUID planId;

    /**
     * Optional — defaults to today. End date is derived from the
     * plan's billing cycle, so it is never accepted from the client.
     */
    private LocalDate startDate;
}
