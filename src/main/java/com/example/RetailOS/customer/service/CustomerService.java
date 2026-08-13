package com.example.RetailOS.customer.service;

import com.example.RetailOS.customer.dto.request.CreateCustomerRequest;
import com.example.RetailOS.customer.dto.request.UpdateCustomerRequest;
import com.example.RetailOS.customer.dto.response.CustomerPurchaseResponse;
import com.example.RetailOS.customer.dto.response.CustomerResponse;
import com.example.RetailOS.customer.entity.Customer;
import com.example.RetailOS.customer.repository.CustomerRepository;
import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceItem;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import com.example.RetailOS.invoice.repository.InvoiceItemRepository;
import com.example.RetailOS.invoice.repository.InvoiceRepository;
import com.example.RetailOS.platform.exception.ResourceAlreadyExistsException;
import com.example.RetailOS.platform.exception.ResourceNotFoundException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final CurrentUserService currentUserService;

    /**
     * Optional search matches name (case-insensitive, contains) or, when
     * the query looks like digits, the phone number.
     */
    public List<CustomerResponse> getCustomers(String search) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Customer> customers;
        if (search == null || search.isBlank()) {
            customers = customerRepository.findAllByShopIdOrderByNameAsc(shopId);
        } else if (search.trim().matches("[0-9+\\-() ]+")) {
            customers = customerRepository
                    .findAllByShopIdAndPhoneContainingOrderByNameAsc(shopId, search.trim());
        } else {
            customers = customerRepository
                    .findAllByShopIdAndNameContainingIgnoreCaseOrderByNameAsc(shopId, search.trim());
        }

        // One pass over the shop's completed invoices instead of a
        // per-customer query, regardless of how many customers matched.
        Map<UUID, PurchaseAggregate> aggregates = purchaseAggregatesByCustomer(shopId);

        return customers.stream()
                .map(customer -> {
                    PurchaseAggregate agg = aggregates.get(customer.getId());
                    return agg == null
                            ? CustomerResponse.from(customer)
                            : CustomerResponse.from(customer, agg.total, agg.lastVisitAt);
                })
                .toList();
    }

    public CustomerResponse getCustomer(UUID id) {
        UUID shopId = currentUserService.getCurrentShopId();
        Customer customer = requireCustomer(id, shopId);

        List<Invoice> completed = invoiceRepository
                .findAllByShopIdAndCustomerIdOrderByCreatedAtDesc(shopId, id).stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.COMPLETED)
                .toList();

        BigDecimal total = completed.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant lastVisitAt = completed.stream()
                .map(Invoice::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return CustomerResponse.from(customer, total, lastVisitAt);
    }

    /**
     * Past completed bills for a customer, newest first — the drawer's
     * purchase history list.
     */
    public List<CustomerPurchaseResponse> getCustomerPurchases(UUID id) {
        UUID shopId = currentUserService.getCurrentShopId();
        requireCustomer(id, shopId);

        List<Invoice> invoices = invoiceRepository
                .findAllByShopIdAndCustomerIdOrderByCreatedAtDesc(shopId, id).stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.COMPLETED)
                .toList();

        if (invoices.isEmpty()) {
            return List.of();
        }

        Map<UUID, Long> itemCounts = invoiceItemRepository
                .findAllByInvoiceIdIn(invoices.stream().map(Invoice::getId).toList()).stream()
                .collect(Collectors.groupingBy(InvoiceItem::getInvoiceId, Collectors.counting()));

        return invoices.stream()
                .map(invoice -> CustomerPurchaseResponse.builder()
                        .id(invoice.getId())
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .itemCount(itemCounts.getOrDefault(invoice.getId(), 0L).intValue())
                        .amount(invoice.getTotalAmount())
                        .createdAt(invoice.getCreatedAt())
                        .build())
                .toList();
    }

    private record PurchaseAggregate(BigDecimal total, Instant lastVisitAt) {}

    private Map<UUID, PurchaseAggregate> purchaseAggregatesByCustomer(UUID shopId) {
        List<Invoice> completed = invoiceRepository
                .findAllByShopIdAndStatusOrderByCreatedAtDesc(shopId, InvoiceStatus.COMPLETED).stream()
                .filter(invoice -> invoice.getCustomerId() != null)
                .toList();

        return completed.stream()
                .collect(Collectors.groupingBy(Invoice::getCustomerId))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    BigDecimal total = entry.getValue().stream()
                            .map(Invoice::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    // Already ordered desc by createdAt per customer, since
                    // the source list is globally ordered that way.
                    Instant lastVisitAt = entry.getValue().get(0).getCreatedAt();
                    return new PurchaseAggregate(total, lastVisitAt);
                }));
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        String phone = normalizePhone(request.getPhone());

        if (phone != null && customerRepository.existsByShopIdAndPhone(shopId, phone)) {
            throw new ResourceAlreadyExistsException(
                    "A customer with this phone number already exists.");
        }

        Customer customer = Customer.builder()
                .shopId(shopId)
                .name(request.getName().trim())
                .phone(phone)
                .build();

        return CustomerResponse.from(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID id, UpdateCustomerRequest request) {

        UUID shopId = currentUserService.getCurrentShopId();
        Customer customer = requireCustomer(id, shopId);

        if (request.getName() != null) {
            customer.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            String phone = normalizePhone(request.getPhone());
            if (phone != null
                    && customerRepository.existsByShopIdAndPhoneAndIdNot(shopId, phone, id)) {
                throw new ResourceAlreadyExistsException(
                        "A customer with this phone number already exists.");
            }
            customer.setPhone(phone);
        }

        return CustomerResponse.from(customerRepository.save(customer));
    }

    /**
     * Find-or-create by phone, used by checkout so a cashier can put a
     * new customer on credit without a separate trip to the Customers
     * screen first. Falls back to creating (phone may be blank — the
     * customer just won't be findable by phone next time).
     */
    @Transactional
    public Customer findOrCreateForCheckout(UUID shopId, String name, String phone) {
        String normalizedPhone = normalizePhone(phone);
        if (normalizedPhone != null) {
            Optional<Customer> existing = customerRepository.findByShopIdAndPhone(shopId, normalizedPhone);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return customerRepository.save(Customer.builder()
                .shopId(shopId)
                .name(name.trim())
                .phone(normalizedPhone)
                .build());
    }

    /**
     * Past invoices and payments keep existing — their DB FKs are
     * ON DELETE SET NULL, so history survives as walk-in records.
     */
    @Transactional
    public void deleteCustomer(UUID id) {
        Customer customer = requireCustomer(id, currentUserService.getCurrentShopId());
        customerRepository.delete(customer);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private Customer requireCustomer(UUID id, UUID shopId) {
        return customerRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found: " + id));
    }
}
