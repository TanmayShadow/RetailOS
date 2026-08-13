-- ==========================================================
-- RetailOS
-- V14 - Add Reference/Invoice Number to Purchases
-- ==========================================================
-- The supplier's own invoice/challan number for this purchase, if they
-- gave one — purely a lookup aid, never validated or deduplicated.
ALTER TABLE purchases
    ADD COLUMN reference_number VARCHAR(50);
