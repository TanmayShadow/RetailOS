package com.example.RetailOS.dashboard.service;

import com.example.RetailOS.customer.entity.Customer;
import com.example.RetailOS.customer.repository.CustomerRepository;
import com.example.RetailOS.dashboard.dto.response.BillPaymentStatus;
import com.example.RetailOS.dashboard.dto.response.DashboardSummaryResponse;
import com.example.RetailOS.dashboard.dto.response.LowStockItemResponse;
import com.example.RetailOS.dashboard.dto.response.RecentBillResponse;
import com.example.RetailOS.dashboard.dto.response.RecentCustomerResponse;
import com.example.RetailOS.dashboard.dto.response.StockSeverity;
import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceItem;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import com.example.RetailOS.invoice.repository.InvoiceItemRepository;
import com.example.RetailOS.invoice.repository.InvoiceRepository;
import com.example.RetailOS.payment.entity.Payment;
import com.example.RetailOS.payment.repository.PaymentRepository;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    /**
     * Stock-to-minimum ratio at or below which a low-stock item is
     * escalated from "Low" to "Very Low".
     */
    private static final BigDecimal VERY_LOW_RATIO = new BigDecimal("0.20");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    public DashboardSummaryResponse getSummary() {

        UUID shopId = currentUserService.getCurrentShopId();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        List<Invoice> todayInvoices = completedInvoicesOn(shopId, today, zone);
        List<Invoice> yesterdayInvoices = completedInvoicesOn(shopId, today.minusDays(1), zone);

        BigDecimal salesToday = sumTotals(todayInvoices);
        BigDecimal salesYesterday = sumTotals(yesterdayInvoices);

        List<Customer> creditCustomers = customerRepository
                .findAllByShopIdAndCurrentBalanceGreaterThanOrderByCurrentBalanceDesc(shopId, BigDecimal.ZERO);
        BigDecimal creditPending = creditCustomers.stream()
                .map(Customer::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Product> lowStock = lowStockProducts(shopId);
        long veryLowCount = lowStock.stream().filter(this::isVeryLow).count();

        return DashboardSummaryResponse.builder()
                .salesToday(salesToday)
                .salesYesterday(salesYesterday)
                .salesChangePercent(percentChange(salesToday, salesYesterday))
                .billsToday(todayInvoices.size())
                .billsYesterday(yesterdayInvoices.size())
                .billsDelta(todayInvoices.size() - yesterdayInvoices.size())
                .creditPending(creditPending)
                .creditPendingCustomerCount(creditCustomers.size())
                .lowStockCount(lowStock.size())
                .veryLowStockCount((int) veryLowCount)
                .build();
    }

    public List<RecentBillResponse> getRecentBills(int limit) {

        UUID shopId = currentUserService.getCurrentShopId();

        // Scoped to today, matching the widget's "Bills created today"
        // subtitle -- completedInvoicesOn already orders desc by createdAt.
        List<Invoice> invoices = completedInvoicesOn(
                        shopId, LocalDate.now(ZoneId.systemDefault()), ZoneId.systemDefault())
                .stream()
                .limit(clamp(limit, 1, 20))
                .toList();

        if (invoices.isEmpty()) {
            return List.of();
        }

        List<UUID> invoiceIds = invoices.stream().map(Invoice::getId).toList();

        Map<UUID, Long> itemCounts = invoiceItemRepository.findAllByInvoiceIdIn(invoiceIds).stream()
                .collect(Collectors.groupingBy(InvoiceItem::getInvoiceId, Collectors.counting()));

        Map<UUID, BigDecimal> paidByInvoice = paymentRepository.findAllByInvoiceIdIn(invoiceIds).stream()
                .collect(Collectors.groupingBy(Payment::getInvoiceId,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        Map<UUID, String> customerNames = customerRepository.findAllById(invoices.stream()
                        .map(Invoice::getCustomerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getName));

        return invoices.stream()
                .map(invoice -> {
                    BigDecimal paid = paidByInvoice.getOrDefault(invoice.getId(), BigDecimal.ZERO);
                    boolean isPaid = invoice.getCustomerId() == null
                            || paid.compareTo(invoice.getTotalAmount()) >= 0;

                    return RecentBillResponse.builder()
                            .id(invoice.getId())
                            .invoiceNumber(invoice.getInvoiceNumber())
                            .customerName(invoice.getCustomerId() == null
                                    ? "Walk-in Customer"
                                    : customerNames.get(invoice.getCustomerId()))
                            .itemCount(itemCounts.getOrDefault(invoice.getId(), 0L).intValue())
                            .amount(invoice.getTotalAmount())
                            .status(isPaid ? BillPaymentStatus.PAID : BillPaymentStatus.CREDIT_DUE)
                            .createdAt(invoice.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public List<LowStockItemResponse> getLowStockItems(int limit) {

        UUID shopId = currentUserService.getCurrentShopId();

        return lowStockProducts(shopId).stream()
                .sorted(Comparator.comparing(this::stockRatio))
                .limit(clamp(limit, 1, 50))
                .map(product -> LowStockItemResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .currentStock(product.getCurrentStock())
                        .minStock(product.getMinStock())
                        .severity(isVeryLow(product) ? StockSeverity.VERY_LOW : StockSeverity.LOW)
                        .build())
                .toList();
    }

    public List<RecentCustomerResponse> getRecentCustomers(int limit) {

        UUID shopId = currentUserService.getCurrentShopId();

        return customerRepository
                .findAllByShopIdOrderByUpdatedAtDesc(shopId, PageRequest.of(0, clamp(limit, 1, 20)))
                .stream()
                .map(customer -> RecentCustomerResponse.builder()
                        .id(customer.getId())
                        .name(customer.getName())
                        .currentBalance(customer.getCurrentBalance())
                        .hasCreditDue(customer.getCurrentBalance().signum() > 0)
                        .build())
                .toList();
    }

    // ---- Helpers ----

    private List<Invoice> completedInvoicesOn(UUID shopId, LocalDate date, ZoneId zone) {
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();
        return invoiceRepository.findAllByShopIdAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                shopId, InvoiceStatus.COMPLETED, start, end);
    }

    private BigDecimal sumTotals(List<Invoice> invoices) {
        return invoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Null when yesterday had no sales — the percentage change is
     * undefined, not zero or infinite.
     */
    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0) {
            return null;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Products at or below their own min-stock threshold. A zero
     * threshold means the owner hasn't opted the product into alerting.
     */
    private List<Product> lowStockProducts(UUID shopId) {
        return productRepository.findAllByShopIdOrderByNameAsc(shopId).stream()
                .filter(p -> p.getMinStock().signum() > 0
                        && p.getCurrentStock().compareTo(p.getMinStock()) <= 0)
                .toList();
    }

    private boolean isVeryLow(Product product) {
        return stockRatio(product).compareTo(VERY_LOW_RATIO) <= 0;
    }

    private BigDecimal stockRatio(Product product) {
        if (product.getMinStock().signum() == 0) {
            return BigDecimal.ZERO;
        }
        return product.getCurrentStock().divide(product.getMinStock(), 4, RoundingMode.HALF_UP);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
