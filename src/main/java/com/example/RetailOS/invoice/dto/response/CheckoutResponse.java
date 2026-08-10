package com.example.RetailOS.invoice.dto.response;

import com.example.RetailOS.payment.entity.PaymentMode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckoutResponse {

    private InvoiceResponse invoice;

    /**
     * The mode the sale was paid in. Always present, including CREDIT —
     * a CREDIT sale has no Payment row (that's what makes it credit),
     * but the mode itself is still meaningful for the receipt/preview.
     */
    private PaymentMode paymentMode;
}
