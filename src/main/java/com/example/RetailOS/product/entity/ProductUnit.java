package com.example.RetailOS.product.entity;

import lombok.Getter;

/**
 * How the product is measured and sold. Count-based units (PIECE, DOZEN,
 * BOX, PACK) only allow whole-number quantities on bills and purchases;
 * measured units (KG, GRAM, LITRE, ML, METER) allow fractions
 * (e.g. 0.250 KG).
 */
@Getter
public enum ProductUnit {

    PIECE(false),
    DOZEN(false),
    BOX(false),
    PACK(false),
    KG(true),
    GRAM(true),
    LITRE(true),
    ML(true),
    METER(true);

    private final boolean fractionalAllowed;

    ProductUnit(boolean fractionalAllowed) {
        this.fractionalAllowed = fractionalAllowed;
    }
}
