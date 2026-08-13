package com.example.RetailOS.report.service;

import com.example.RetailOS.invoice.entity.Invoice;
import com.example.RetailOS.invoice.entity.InvoiceItem;
import com.example.RetailOS.invoice.entity.InvoiceStatus;
import com.example.RetailOS.invoice.repository.InvoiceItemRepository;
import com.example.RetailOS.invoice.repository.InvoiceRepository;
import com.example.RetailOS.payment.entity.Payment;
import com.example.RetailOS.payment.entity.PaymentMode;
import com.example.RetailOS.payment.repository.PaymentRepository;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.repository.ProductRepository;
import com.example.RetailOS.report.dto.response.DailySalesResponse;
import com.example.RetailOS.report.dto.response.PaymentMethodBreakdownResponse;
import com.example.RetailOS.report.dto.response.ReportSummaryResponse;
import com.example.RetailOS.report.dto.response.TopProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public ReportSummaryResponse getSummary(LocalDate from, LocalDate to) {
        UUID shopId = currentUserService.getCurrentShopId();

        List<Invoice> current = invoicesBetween(shopId, from, to);
        List<Invoice> previous = invoicesBetween(shopId, priorFrom(from, to), from.minusDays(1));

        BigDecimal totalSales = sumTotals(current);
        BigDecimal prevTotalSales = sumTotals(previous);

        int billsCreated = current.size();
        int prevBillsCreated = previous.size();

        BigDecimal grossProfit = grossProfitOf(current);
        BigDecimal margin = totalSales.signum() == 0
                ? BigDecimal.ZERO
                : grossProfit.divide(totalSales, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);

        BigDecimal avgBillValue = billsCreated == 0
                ? BigDecimal.ZERO
                : totalSales.divide(BigDecimal.valueOf(billsCreated), 2, RoundingMode.HALF_UP);
        BigDecimal prevAvgBillValue = prevBillsCreated == 0
                ? BigDecimal.ZERO
                : prevTotalSales.divide(BigDecimal.valueOf(prevBillsCreated), 2, RoundingMode.HALF_UP);

        return ReportSummaryResponse.builder()
                .totalSales(totalSales)
                .totalSalesDeltaPercent(percentChange(totalSales, prevTotalSales))
                .billsCreated(billsCreated)
                .billsCreatedDelta(billsCreated - prevBillsCreated)
                .grossProfit(grossProfit)
                .grossProfitMarginPercent(margin)
                .avgBillValue(avgBillValue)
                .avgBillValueDeltaPercent(percentChange(avgBillValue, prevAvgBillValue))
                .build();
    }

    public List<DailySalesResponse> getSalesTrend(LocalDate from, LocalDate to) {
        UUID shopId = currentUserService.getCurrentShopId();
        List<Invoice> invoices = invoicesBetween(shopId, from, to);
        ZoneId zone = ZoneId.systemDefault();

        Map<LocalDate, BigDecimal> byDay = invoices.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getCreatedAt().atZone(zone).toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getTotalAmount, BigDecimal::add)));

        return from.datesUntil(to.plusDays(1))
                .map(date -> DailySalesResponse.builder()
                        .date(date)
                        .totalSales(byDay.getOrDefault(date, BigDecimal.ZERO))
                        .build())
                .toList();
    }

    public List<PaymentMethodBreakdownResponse> getPaymentMethods(LocalDate from, LocalDate to) {
        UUID shopId = currentUserService.getCurrentShopId();
        List<Payment> payments = paymentRepository.findAllByShopIdAndPaymentDateBetween(shopId, from, to);

        Map<PaymentMode, BigDecimal> byMode = payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentMode,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmount, BigDecimal::add)));

        BigDecimal total = byMode.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        return List.of(PaymentMode.CASH, PaymentMode.UPI, PaymentMode.CARD, PaymentMode.CREDIT).stream()
                .map(mode -> {
                    BigDecimal amount = byMode.getOrDefault(mode, BigDecimal.ZERO);
                    BigDecimal percent = total.signum() == 0
                            ? BigDecimal.ZERO
                            : amount.divide(total, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
                    return PaymentMethodBreakdownResponse.builder()
                            .mode(mode)
                            .amount(amount)
                            .percent(percent)
                            .build();
                })
                .toList();
    }

    public List<TopProductResponse> getTopProducts(LocalDate from, LocalDate to, int limit) {
        UUID shopId = currentUserService.getCurrentShopId();
        List<Invoice> invoices = invoicesBetween(shopId, from, to);
        if (invoices.isEmpty()) {
            return List.of();
        }

        List<InvoiceItem> items = invoiceItemRepository
                .findAllByInvoiceIdIn(invoices.stream().map(Invoice::getId).toList()).stream()
                .filter(i -> i.getProductId() != null)
                .toList();

        Map<UUID, Product> productsById = productRepository
                .findAllById(items.stream().map(InvoiceItem::getProductId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return items.stream()
                .collect(Collectors.groupingBy(InvoiceItem::getProductId))
                .entrySet().stream()
                .map(entry -> {
                    Product product = productsById.get(entry.getKey());
                    BigDecimal unitsSold = entry.getValue().stream()
                            .map(InvoiceItem::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal revenue = entry.getValue().stream()
                            .map(InvoiceItem::getLineTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal cost = product == null
                            ? BigDecimal.ZERO
                            : unitsSold.multiply(product.getPurchasePrice()).setScale(2, RoundingMode.HALF_UP);
                    return TopProductResponse.builder()
                            .productId(entry.getKey())
                            .productName(product == null ? "Deleted product" : product.getName())
                            .unitsSold(unitsSold)
                            .revenue(revenue)
                            .profit(revenue.subtract(cost))
                            .build();
                })
                .sorted(Comparator.comparing(TopProductResponse::getUnitsSold).reversed())
                .limit(Math.max(1, Math.min(limit, 50)))
                .toList();
    }

    // ---- Helpers ----

    private List<Invoice> invoicesBetween(UUID shopId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return List.of();
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant start = from.atStartOfDay(zone).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(zone).toInstant();
        return invoiceRepository.findAllByShopIdAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                shopId, InvoiceStatus.COMPLETED, start, end);
    }

    /** An immediately-preceding window of equal length, for period-over-period deltas. */
    private LocalDate priorFrom(LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        return from.minusDays(days);
    }

    private BigDecimal sumTotals(List<Invoice> invoices) {
        return invoices.stream().map(Invoice::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal grossProfitOf(List<Invoice> invoices) {
        if (invoices.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<InvoiceItem> items = invoiceItemRepository
                .findAllByInvoiceIdIn(invoices.stream().map(Invoice::getId).toList()).stream()
                .filter(i -> i.getProductId() != null)
                .toList();

        Map<UUID, Product> productsById = productRepository
                .findAllById(items.stream().map(InvoiceItem::getProductId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return items.stream()
                .map(item -> {
                    Product product = productsById.get(item.getProductId());
                    BigDecimal cost = product == null
                            ? BigDecimal.ZERO
                            : item.getQuantity().multiply(product.getPurchasePrice())
                                    .setScale(2, RoundingMode.HALF_UP);
                    return item.getLineTotal().subtract(cost);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Null when the prior value is zero — percent change is undefined, not zero or infinite. */
    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous.signum() == 0) {
            return null;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
