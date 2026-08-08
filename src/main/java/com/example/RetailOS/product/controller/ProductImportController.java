package com.example.RetailOS.product.controller;

import com.example.RetailOS.product.dto.request.ImportCommitRequest;
import com.example.RetailOS.product.dto.response.ImportCommitResponse;
import com.example.RetailOS.product.dto.response.ImportPreviewResponse;
import com.example.RetailOS.product.service.ProductImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products/import")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService productImportService;

    /**
     * Parses the uploaded workbook and returns a row-by-row preview with
     * validation/duplicate flags. Nothing is written to the database here.
     */
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ImportPreviewResponse> preview(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(productImportService.preview(file));
    }

    /**
     * Creates a product for each confirmed row. One row failing (e.g. a
     * duplicate created since the preview was generated) doesn't abort
     * the rest of the batch.
     */
    @PostMapping("/commit")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ImportCommitResponse> commit(@Valid @RequestBody ImportCommitRequest request) {
        return ResponseEntity.ok(productImportService.commit(request));
    }

    @GetMapping("/template")
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<byte[]> template() {
        byte[] bytes = productImportService.generateTemplate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product-import-template.xlsx")
                .body(bytes);
    }
}
