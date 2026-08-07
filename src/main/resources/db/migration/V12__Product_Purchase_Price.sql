-- ==========================================================
-- RetailOS
-- V12 - Add Purchase Price to Products
-- ==========================================================
-- Reference/default purchase price shown alongside selling price on the
-- Products screen. Independent of purchase_items.purchase_price, which
-- records the actual price paid on each individual purchase transaction.
ALTER TABLE products
    ADD COLUMN purchase_price NUMERIC(10,2) NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_purchase_price
        CHECK (purchase_price >= 0);
