package com.example.RetailOS.payment.service;

import com.example.RetailOS.customer.entity.Customer;
import com.example.RetailOS.customer.repository.CustomerRepository;
import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import com.example.RetailOS.invoice.repository.InvoiceRepository;
import com.example.RetailOS.payment.dto.request.CreatePaymentRequest;
import com.example.RetailOS.payment.dto.response.PaymentResponse;
import com.example.RetailOS.payment.entity.Payment;
import com.example.RetailOS.payment.entity.PaymentMode;
import com.example.RetailOS.payment.repository.PaymentRepository;
import com.example.RetailOS.platform.exception.BadRequestException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    /**
     * Records a payment; when a customer is involved (given directly or
     * taken from the invoice) their outstanding balance drops by the
     * amount in the same transaction — the app-level equivalent of the
     * target schema's trg_payments_balance. Payments are immutable once
     * recorded: there are no update or delete endpoints.
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();

        UUID invoiceId = null;
        UUID customerId = request.getCustomerId();

        if (request.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository
                    .findByIdAndShopId(request.getInvoiceId(), shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Invoice not found: " + request.getInvoiceId()));
            if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
                throw new BadRequestException("Cannot record a payment against a cancelled invoice.");
            }
            invoiceId = invoice.getId();
            if (customerId == null) {
                customerId = invoice.getCustomerId();
            }
        }

        Customer customer = null;
        if (customerId != null) {
            customer = customerRepository.findByIdAndShopId(customerId, shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found: " + request.getCustomerId()));
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .shopId(shopId)
                .invoiceId(invoiceId)
                .customerId(customerId)
                .amount(request.getAmount())
                .paymentMode(request.getPaymentMode() == null
                        ? PaymentMode.CASH : request.getPaymentMode())
                .paymentDate(request.getPaymentDate() == null
                        ? LocalDate.now() : request.getPaymentDate())
                .build());

        if (customer != null) {
            customer.setCurrentBalance(customer.getCurrentBalance().subtract(payment.getAmount()));
            customerRepository.save(customer);
        }

        return PaymentResponse.from(payment, customer == null ? null : customer.getName());
    }

    public List<PaymentResponse> getPayments(UUID customerId, UUID invoiceId) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Payment> payments;
        if (invoiceId != null) {
            payments = paymentRepository
                    .findAllByShopIdAndInvoiceIdOrderByPaymentDateDescCreatedAtDesc(shopId, invoiceId);
        } else if (customerId != null) {
            payments = paymentRepository
                    .findAllByShopIdAndCustomerIdOrderByPaymentDateDescCreatedAtDesc(shopId, customerId);
        } else {
            payments = paymentRepository.findAllByShopIdOrderByPaymentDateDescCreatedAtDesc(shopId);
        }

        Map<UUID, String> customerNames = customerRepository
                .findAllById(payments.stream()
                        .map(Payment::getCustomerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));

        return payments.stream()
                .map(p -> PaymentResponse.from(p, customerNames.get(p.getCustomerId())))
                .toList();
    }

    public PaymentResponse getPayment(UUID id) {
        Payment payment = paymentRepository
                .findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found: " + id));

        String customerName = payment.getCustomerId() == null
                ? null
                : customerRepository.findById(payment.getCustomerId())
                        .map(Customer::getName).orElse(null);

        return PaymentResponse.from(payment, customerName);
    }
}
