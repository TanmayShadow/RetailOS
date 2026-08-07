package com.example.RetailOS.product.service;

import com.example.RetailOS.platform.exception.BadRequestException;
import com.example.RetailOS.platform.security.service.CurrentUserService;
import com.example.RetailOS.product.dto.request.CreateProductRequest;
import com.example.RetailOS.product.dto.request.ImportCommitRequest;
import com.example.RetailOS.product.dto.request.ImportRowRequest;
import com.example.RetailOS.product.dto.response.ImportCommitResponse;
import com.example.RetailOS.product.dto.response.ImportPreviewResponse;
import com.example.RetailOS.product.dto.response.ImportRowResult;
import com.example.RetailOS.product.dto.response.ImportRowStatus;
import com.example.RetailOS.product.entity.Product;
import com.example.RetailOS.product.entity.ProductUnit;
import com.example.RetailOS.product.repository.ProductRepository;
import com.example.RetailOS.platform.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    private static final List<String> TEMPLATE_HEADERS = List.of(
            "Name", "SKU", "Category", "Unit",
            "Purchase Price", "Selling Price", "Opening Stock", "Min Stock");

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CurrentUserService currentUserService;

    public ImportPreviewResponse preview(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a .xlsx file to import.");
        }

        UUID shopId = currentUserService.getCurrentShopId();

        Set<String> existingNames = new HashSet<>();
        Set<String> existingSkus = new HashSet<>();
        for (Product product : productRepository.findAllByShopIdOrderByNameAsc(shopId)) {
            existingNames.add(normalize(product.getName()));
            if (product.getSku() != null && !product.getSku().isBlank()) {
                existingSkus.add(normalize(product.getSku()));
            }
        }

        List<Map<String, String>> rawRows = readRows(file);

        List<ImportRowResult> results = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        Set<String> seenSkus = new HashSet<>();

        int rowNumber = 0;
        for (Map<String, String> raw : rawRows) {
            rowNumber++;
            ImportRowResult result = validateRow(rowNumber, raw, existingNames, existingSkus, seenNames, seenSkus);
            results.add(result);
        }

        return ImportPreviewResponse.builder()
                .rows(results)
                .validCount((int) results.stream().filter(r -> r.getStatus() == ImportRowStatus.VALID).count())
                .duplicateCount((int) results.stream().filter(r -> r.getStatus() == ImportRowStatus.DUPLICATE).count())
                .errorCount((int) results.stream().filter(r -> r.getStatus() == ImportRowStatus.ERROR).count())
                .build();
    }

    public ImportCommitResponse commit(ImportCommitRequest request) {

        int created = 0;
        List<String> errors = new ArrayList<>();

        for (ImportRowRequest row : request.getRows()) {
            try {
                CreateProductRequest createRequest = new CreateProductRequest();
                createRequest.setName(row.getName());
                createRequest.setSku(row.getSku());
                createRequest.setUnit(parseUnit(row.getUnit()));
                createRequest.setPurchasePrice(row.getPurchasePrice());
                createRequest.setSellingPrice(row.getSellingPrice());
                createRequest.setOpeningStock(row.getOpeningStock());
                createRequest.setMinStock(row.getMinStock());
                createRequest.setCategoryName(row.getCategoryName());

                productService.create(createRequest);
                created++;
            } catch (ApiException ex) {
                errors.add(row.getName() + ": " + ex.getMessage());
            } catch (Exception ex) {
                errors.add(row.getName() + ": could not be imported.");
            }
        }

        return ImportCommitResponse.builder()
                .createdCount(created)
                .failedCount(errors.size())
                .errors(errors)
                .build();
    }

    public byte[] generateTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Products");

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);

            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS.get(i));
                cell.setCellStyle(headerStyle);
            }

            Object[] example = {
                    "Parle-G Biscuit 500g", "GRO-0231", "Grocery", "PIECE", 32, 40, 100, 20
            };
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < example.length; i++) {
                Cell cell = exampleRow.createCell(i);
                if (example[i] instanceof Number number) {
                    cell.setCellValue(number.doubleValue());
                } else {
                    cell.setCellValue(String.valueOf(example[i]));
                }
            }

            for (int i = 0; i < TEMPLATE_HEADERS.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Could not generate the import template.", ex);
        }
    }

    // ---- Parsing ----

    private List<Map<String, String>> readRows(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                return List.of();
            }

            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> columnKeys = new HashMap<>();
            for (Cell cell : headerRow) {
                columnKeys.put(cell.getColumnIndex(), normalizeHeader(formatter.formatCellValue(cell)));
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }
                Map<String, String> values = new HashMap<>();
                for (Cell cell : row) {
                    String key = columnKeys.get(cell.getColumnIndex());
                    if (key != null) {
                        values.put(key, formatter.formatCellValue(cell).trim());
                    }
                }
                rows.add(values);
            }
            return rows;
        } catch (IOException | RuntimeException ex) {
            throw new BadRequestException("Could not read that file — make sure it's a valid .xlsx export.");
        }
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK && !formatter.formatCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String normalizeHeader(String header) {
        return header.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private ImportRowResult validateRow(
            int rowNumber,
            Map<String, String> raw,
            Set<String> existingNames,
            Set<String> existingSkus,
            Set<String> seenNames,
            Set<String> seenSkus
    ) {
        List<String> messages = new ArrayList<>();

        String name = raw.getOrDefault("name", "").trim();
        String sku = raw.getOrDefault("sku", "").trim();
        String category = raw.getOrDefault("category", "").trim();
        String unitRaw = raw.getOrDefault("unit", "").trim();

        if (name.isBlank()) {
            messages.add("Name is required.");
        }

        ProductUnit unit = null;
        if (!unitRaw.isBlank()) {
            unit = parseUnit(unitRaw);
            if (unit == null) {
                messages.add("Unit \"" + unitRaw + "\" isn't recognized. Use one of: "
                        + String.join(", ", Arrays.stream(ProductUnit.values())
                                .map(Enum::name).toList()) + ", or leave it blank for PIECE.");
            }
        }

        BigDecimal purchasePrice = parseMoney(raw.get("purchaseprice"), messages, "Purchase Price");
        BigDecimal sellingPrice = parseMoney(raw.get("sellingprice"), messages, "Selling Price");
        BigDecimal openingStock = parseOptionalNumber(raw.get("openingstock"), messages, "Opening Stock");
        BigDecimal minStock = parseOptionalNumber(raw.get("minstock"), messages, "Min Stock");

        ImportRowStatus status = messages.isEmpty() ? ImportRowStatus.VALID : ImportRowStatus.ERROR;

        if (status == ImportRowStatus.VALID) {
            String normalizedName = normalize(name);
            String normalizedSku = sku.isBlank() ? null : normalize(sku);

            boolean isDuplicate = existingNames.contains(normalizedName)
                    || seenNames.contains(normalizedName)
                    || (normalizedSku != null && (existingSkus.contains(normalizedSku) || seenSkus.contains(normalizedSku)));

            if (isDuplicate) {
                status = ImportRowStatus.DUPLICATE;
                messages.add("A product with this name or SKU already exists.");
            } else {
                seenNames.add(normalizedName);
                if (normalizedSku != null) {
                    seenSkus.add(normalizedSku);
                }
            }
        }

        return ImportRowResult.builder()
                .rowNumber(rowNumber)
                .name(name)
                .sku(sku.isBlank() ? null : sku)
                .categoryName(category.isBlank() ? null : category)
                .unit((unit == null ? ProductUnit.PIECE : unit).name())
                .purchasePrice(purchasePrice)
                .sellingPrice(sellingPrice)
                .openingStock(openingStock == null ? BigDecimal.ZERO : openingStock)
                .minStock(minStock == null ? BigDecimal.ZERO : minStock)
                .status(status)
                .messages(messages)
                .build();
    }

    private BigDecimal parseMoney(String raw, List<String> messages, String label) {
        if (raw == null || raw.isBlank()) {
            messages.add(label + " is required.");
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.signum() < 0) {
                messages.add(label + " can't be negative.");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            messages.add(label + " \"" + raw + "\" isn't a valid number.");
            return null;
        }
    }

    private BigDecimal parseOptionalNumber(String raw, List<String> messages, String label) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.signum() < 0) {
                messages.add(label + " can't be negative.");
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            messages.add(label + " \"" + raw + "\" isn't a valid number.");
            return null;
        }
    }

    private ProductUnit parseUnit(String raw) {
        if (raw == null || raw.isBlank()) {
            return ProductUnit.PIECE;
        }
        try {
            return ProductUnit.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
