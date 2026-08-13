package com.example.RetailOS.report.dto.response;

import com.example.RetailOS.payment.entity.PaymentMode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentMethodBreakdownResponse {
    private PaymentMode mode;
    private BigDecimal amount;
    /** 0-100, share of the total across all methods in the range. */
    private BigDecimal percent;
}
