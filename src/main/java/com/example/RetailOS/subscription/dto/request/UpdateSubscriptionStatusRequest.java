package com.example.RetailOS.subscription.dto.request;

import com.example.RetailOS.subscription.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubscriptionStatusRequest {

    @NotNull
    private SubscriptionStatus status;
}
