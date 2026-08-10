-- ==========================================================
-- RetailOS
-- V13 - Add Discount Amount to Invoices
-- ==========================================================
-- total_amount already represents the grand total (post-discount);
-- this column exists purely so the subtotal/discount breakdown can be
-- reconstructed later (e.g. on invoice preview/reprint).
ALTER TABLE invoices
    ADD COLUMN discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0;

ALTER TABLE invoices
    ADD CONSTRAINT chk_invoices_discount_amount
        CHECK (discount_amount >= 0);
