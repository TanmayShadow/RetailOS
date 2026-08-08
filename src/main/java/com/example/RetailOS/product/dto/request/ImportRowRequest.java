package com.example.RetailOS.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * One row the user confirmed on the import preview screen, echoed back
 * from the frontend (parsing happens once, on /import/preview — nothing
 * is held in server-side session state between preview and commit).
 */
@Getter
@Setter
public class ImportRowRequest {

    @NotBlank
    @Size(min = 2, max = 150)
    private String name;

    @Size(max = 50)
    private String sku;

    @Size(max = 100)
    private String categoryName;

    /** Name of a ProductUnit value, e.g. "PIECE" — defaults to PIECE if blank. */
    private String unit;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal purchasePrice;

    @NotNull
    @DecimalMin(value = "0.00")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.00")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal openingStock;

    @DecimalMin(value = "0.00")
    @Digits(integer = 9, fraction = 3)
    private BigDecimal minStock;
}
