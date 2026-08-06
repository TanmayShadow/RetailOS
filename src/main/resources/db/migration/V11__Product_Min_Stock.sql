-- ==========================================================
-- RetailOS
-- V11 - Add Minimum Stock Threshold to Products
-- ==========================================================
-- Low-stock alerting threshold; existing rows default to 0 (no alert
-- until an owner sets one).
ALTER TABLE products
    ADD COLUMN min_stock NUMERIC(12,3) NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_min_stock
        CHECK (min_stock >= 0);
