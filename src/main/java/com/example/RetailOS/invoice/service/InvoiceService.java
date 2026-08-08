package com.example.RetailOS.invoice.service;

import com.example.RetailOS.customer.entity.Customer;
import com.example.RetailOS.customer.repository.CustomerRepository;
import com.example.RetailOS.customer.service.CustomerService;
import com.example.RetailOS.invoice.dto.request.CheckoutRequest;
import com.example.RetailOS.invoice.dto.request.CreateInvoiceRequest;
import com.example.RetailOS.invoice.dto.request.InvoiceItemRequest;
import com.example.RetailOS.invoice.dto.request.UpdateInvoiceRequest;
import com.example.RetailOS.invoice.dto.response.CheckoutResponse;
import com.example.RetailOS.invoice.dto.response.InvoiceItemResponse;
import com.example.RetailOS.invoice.dto.response.InvoiceResponse;
import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceItem;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import com.example.RetailOS.invoice.repository.InvoiceItemRepository;
import com.example.RetailOS.invoice.repository.InvoiceRepository;
import com.example.RetailOS.payment.dto.request.CreatePaymentRequest;
import com.example.RetailOS.payment.entity.PaymentMode;
import com.example.RetailOS.payment.service.PaymentService;
import com.example.RetailOS.platform.exception.BadRequestException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.repository.ProductRepository;
import com.example.RetailOS.shop.entity.Shop;
import com.example.RetailOS.shop.repository.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final Set<InvoiceStatus> EDITABLE_STATUSES =
            Set.of(InvoiceStatus.DRAFT, InvoiceStatus.HELD);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final PaymentService paymentService;
    private final ShopRepository shopRepository;
    private final CurrentUserService currentUserService;

    /**
     * Creates a DRAFT invoice. No stock or balance moves here — those
     * effects fire once, on completion.
     */
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        UUID customerId = resolveCustomerId(shopId, request.getCustomerId());

        Invoice invoice = invoiceRepository.save(Invoice.builder()
                .shopId(shopId)
                .customerId(customerId)
                .invoiceNumber(generateInvoiceNumber(shopId))
                .build());

        List<InvoiceItem> items = buildItems(shopId, invoice.getId(), request.getItems());
        invoiceItemRepository.saveAll(items);

        invoice.setTotalAmount(sumLineTotals(items));
        invoice = invoiceRepository.save(invoice);

        return toDetailResponse(invoice, items);
    }

    /**
     * The Billing screen's single "finalize sale" action: creates the
     * invoice already COMPLETED, decrements stock, applies the discount,
     * and (unless paying on credit) records full payment — all in one
     * transaction. There's no draft/hold step in front of this; the
     * cart lives in the browser until the cashier checks out.
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        UUID customerId = resolveCheckoutCustomerId(shopId, request);

        Invoice invoice = invoiceRepository.save(Invoice.builder()
                .shopId(shopId)
                .customerId(customerId)
                .invoiceNumber(generateInvoiceNumber(shopId))
                .build());

        List<InvoiceItem> items = buildItems(shopId, invoice.getId(), request.getItems());
        invoiceItemRepository.saveAll(items);

        BigDecimal subtotal = sumLineTotals(items);
        BigDecimal discount = request.getDiscountAmount() == null
                ? BigDecimal.ZERO : request.getDiscountAmount();
        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount can't exceed the subtotal.");
        }
        BigDecimal grandTotal = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        // Stock is decremented permissively here (no overselling block) —
        // a shop may sell ahead of what the system shows in stock.
        for (InvoiceItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setCurrentStock(product.getCurrentStock().subtract(item.getQuantity()));
                productRepository.save(product);
            });
        }

        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found: " + customerId));
            customer.setCurrentBalance(customer.getCurrentBalance().add(grandTotal));
            customerRepository.save(customer);
        }

        invoice.setDiscountAmount(discount);
        invoice.setTotalAmount(grandTotal);
        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice = invoiceRepository.save(invoice);

        if (request.getPaymentMode() != PaymentMode.CREDIT && grandTotal.signum() > 0) {
            CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
            paymentRequest.setInvoiceId(invoice.getId());
            paymentRequest.setCustomerId(customerId);
            paymentRequest.setAmount(grandTotal);
            paymentRequest.setPaymentMode(request.getPaymentMode());
            paymentService.createPayment(paymentRequest);
        }

        return CheckoutResponse.builder()
                .invoice(toDetailResponse(invoice, items))
                .paymentMode(request.getPaymentMode())
                .build();
    }

    /**
     * Edits a DRAFT/HELD invoice (hold-and-resume / autosave). A non-null
     * items list replaces all existing items and recomputes the total.
     */
    @Transactional
    public InvoiceResponse updateInvoice(UUID id, UpdateInvoiceRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        Invoice invoice = requireEditable(requireInvoice(id));

        if (Boolean.TRUE.equals(request.getRemoveCustomer())) {
            invoice.setCustomerId(null);
        } else if (request.getCustomerId() != null) {
            invoice.setCustomerId(resolveCustomerId(shopId, request.getCustomerId()));
        }

        List<InvoiceItem> items;
        if (request.getItems() != null) {
            if (request.getItems().isEmpty()) {
                throw new BadRequestException("An invoice must have at least one item.");
            }
            invoiceItemRepository.deleteAllByInvoiceId(id);
            items = buildItems(shopId, id, request.getItems());
            invoiceItemRepository.saveAll(items);
            invoice.setTotalAmount(sumLineTotals(items));
        } else {
            items = invoiceItemRepository.findAllByInvoiceId(id);
        }

        return toDetailResponse(invoiceRepository.save(invoice), items);
    }

    public List<InvoiceResponse> getInvoices(InvoiceStatus status, UUID customerId) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Invoice> invoices;
        if (status != null) {
            invoices = invoiceRepository.findAllByShopIdAndStatusOrderByCreatedAtDesc(shopId, status);
        } else if (customerId != null) {
            invoices = invoiceRepository.findAllByShopIdAndCustomerIdOrderByCreatedAtDesc(shopId, customerId);
        } else {
            invoices = invoiceRepository.findAllByShopIdOrderByCreatedAtDesc(shopId);
        }

        Map<UUID, String> customerNames = customerNamesById(invoices);
        return invoices.stream()
                .map(i -> InvoiceResponse.from(i, customerNames.get(i.getCustomerId()), null))
                .toList();
    }

    public InvoiceResponse getInvoice(UUID id) {
        Invoice invoice = requireInvoice(id);
        return toDetailResponse(invoice, invoiceItemRepository.findAllByInvoiceId(id));
    }

    @Transactional
    public InvoiceResponse holdInvoice(UUID id) {
        Invoice invoice = requireInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BadRequestException("Only a DRAFT invoice can be held.");
        }
        invoice.setStatus(InvoiceStatus.HELD);
        return toDetailResponse(invoiceRepository.save(invoice),
                invoiceItemRepository.findAllByInvoiceId(id));
    }

    @Transactional
    public InvoiceResponse resumeInvoice(UUID id) {
        Invoice invoice = requireInvoice(id);
        if (invoice.getStatus() != InvoiceStatus.HELD) {
            throw new BadRequestException("Only a HELD invoice can be resumed.");
        }
        invoice.setStatus(InvoiceStatus.DRAFT);
        return toDetailResponse(invoiceRepository.save(invoice),
                invoiceItemRepository.findAllByInvoiceId(id));
    }

    /**
     * The DRAFT/HELD -> COMPLETED transition, with the effects the target
     * schema implements as trg_invoice_completed: each item's quantity
     * comes off product stock, and for a credit (customer-linked) sale the
     * customer's balance goes up by the invoice total. Fires exactly once —
     * a COMPLETED invoice can never be completed again.
     */
    @Transactional
    public InvoiceResponse completeInvoice(UUID id) {

        Invoice invoice = requireEditable(requireInvoice(id));
        List<InvoiceItem> items = invoiceItemRepository.findAllByInvoiceId(id);
        if (items.isEmpty()) {
            throw new BadRequestException("An invoice must have at least one item.");
        }

        for (InvoiceItem item : items) {
            if (item.getProductId() == null) {
                continue;
            }
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setCurrentStock(product.getCurrentStock().subtract(item.getQuantity()));
                productRepository.save(product);
            });
        }

        if (invoice.getCustomerId() != null) {
            Customer customer = customerRepository.findById(invoice.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Customer not found: " + invoice.getCustomerId()));
            customer.setCurrentBalance(customer.getCurrentBalance().add(invoice.getTotalAmount()));
            customerRepository.save(customer);
        }

        invoice.setStatus(InvoiceStatus.COMPLETED);
        return toDetailResponse(invoiceRepository.save(invoice), items);
    }

    /**
     * Only DRAFT/HELD invoices can be cancelled — they never touched stock
     * or balances, so there is nothing to reverse. A COMPLETED invoice is
     * immutable; corrections belong in a future credit-note flow.
     */
    @Transactional
    public InvoiceResponse cancelInvoice(UUID id) {
        Invoice invoice = requireEditable(requireInvoice(id));
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return toDetailResponse(invoiceRepository.save(invoice),
                invoiceItemRepository.findAllByInvoiceId(id));
    }

    // ---- Helpers ----

    private List<InvoiceItem> buildItems(UUID shopId, UUID invoiceId,
                                         List<InvoiceItemRequest> itemRequests) {
        List<InvoiceItem> items = new ArrayList<>();
        for (InvoiceItemRequest itemRequest : itemRequests) {

            Product product = productRepository
                    .findByIdAndShopId(itemRequest.getProductId(), shopId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemRequest.getProductId()));

            if (!product.getUnit().isFractionalAllowed()
                    && itemRequest.getQuantity().stripTrailingZeros().scale() > 0) {
                throw new BadRequestException(
                        product.getName() + " is sold per " + product.getUnit()
                                + " — quantity must be a whole number.");
            }

            BigDecimal lineTotal = itemRequest.getQuantity()
                    .multiply(product.getSellingPrice())
                    .setScale(2, RoundingMode.HALF_UP);

            items.add(InvoiceItem.builder()
                    .invoiceId(invoiceId)
                    .productId(product.getId())
                    .quantity(itemRequest.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }
        return items;
    }

    private BigDecimal sumLineTotals(List<InvoiceItem> items) {
        return items.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateInvoiceNumber(UUID shopId) {
        String prefix = shopRepository.findById(shopId)
                .map(Shop::getInvoicePrefix)
                .orElse("INV");
        long next = invoiceRepository.countByShopId(shopId) + 1;
        String number = String.format("%s-%06d", prefix, next);
        while (invoiceRepository.existsByShopIdAndInvoiceNumber(shopId, number)) {
            number = String.format("%s-%06d", prefix, ++next);
        }
        return number;
    }

    private UUID resolveCustomerId(UUID shopId, UUID customerId) {
        if (customerId == null) {
            return null;
        }
        return customerRepository.findByIdAndShopId(customerId, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + customerId))
                .getId();
    }

    /**
     * An explicitly picked customer always wins. Otherwise, a credit sale
     * needs a real customer to carry the balance — find-or-create one from
     * the typed name/phone. Cash/UPI/Card walk-ins never get a customer
     * record just from a typed name.
     */
    private UUID resolveCheckoutCustomerId(UUID shopId, CheckoutRequest request) {
        if (request.getCustomerId() != null) {
            return resolveCustomerId(shopId, request.getCustomerId());
        }
        if (request.getPaymentMode() == PaymentMode.CREDIT
                && request.getCustomerName() != null
                && !request.getCustomerName().isBlank()) {
            return customerService
                    .findOrCreateForCheckout(shopId, request.getCustomerName(), request.getCustomerPhone())
                    .getId();
        }
        return null;
    }

    private Invoice requireInvoice(UUID id) {
        return invoiceRepository.findByIdAndShopId(id, currentUserService.getCurrentShopId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found: " + id));
    }

    private Invoice requireEditable(Invoice invoice) {
        if (!EDITABLE_STATUSES.contains(invoice.getStatus())) {
            throw new BadRequestException(
                    "Invoice " + invoice.getInvoiceNumber() + " is "
                            + invoice.getStatus() + " and can no longer be changed.");
        }
        return invoice;
    }

    private InvoiceResponse toDetailResponse(Invoice invoice, List<InvoiceItem> items) {

        Map<UUID, Product> productsById = productRepository
                .findAllById(items.stream()
                        .map(InvoiceItem::getProductId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        String customerName = invoice.getCustomerId() == null
                ? null
                : customerRepository.findById(invoice.getCustomerId())
                        .map(Customer::getName).orElse(null);

        return InvoiceResponse.from(invoice, customerName,
                items.stream()
                        .map(i -> InvoiceItemResponse.from(i,
                                i.getProductId() == null ? null : productsById.get(i.getProductId())))
                        .toList());
    }

    private Map<UUID, String> customerNamesById(List<Invoice> invoices) {
        return customerRepository
                .findAllById(invoices.stream()
                        .map(Invoice::getCustomerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));
    }
}
