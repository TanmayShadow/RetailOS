-- ==========================================================
-- RetailOS
-- V10 - Add Unit Type to Products
-- ==========================================================
-- How the product is measured and sold (selling price is per unit).
-- Existing rows default to PIECE.
ALTER TABLE products
    ADD COLUMN unit_type VARCHAR(10) NOT NULL DEFAULT 'PIECE';

ALTER TABLE products
    ADD CONSTRAINT chk_products_unit_type
        CHECK (unit_type IN ('PIECE', 'DOZEN', 'BOX', 'PACK',
                             'KG', 'GRAM', 'LITRE', 'ML', 'METER'));
