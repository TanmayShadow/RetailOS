-- ==========================================================
-- RetailOS
-- V4 - Add Shops Table
-- ==========================================================
CREATE TABLE shops (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    gst_in VARCHAR(15),
    logo_url TEXT,
    invoice_prefix VARCHAR(20) NOT NULL DEFAULT 'INV',
    invoice_footer TEXT,
    whatsapp_number VARCHAR(15),
    upi_id VARCHAR(100),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);