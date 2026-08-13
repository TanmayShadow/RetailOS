-- ==========================================================
-- RetailOS
-- V15 - Add Address and Invoice Watermark Toggle to Shops
-- ==========================================================
ALTER TABLE shops
    ADD COLUMN address VARCHAR(255);

-- Whether the "PAID" stamp renders on the Invoice Preview — existing
-- shops default to on, matching current behavior before this toggle existed.
ALTER TABLE shops
    ADD COLUMN show_watermark BOOLEAN NOT NULL DEFAULT true;
