-- ==========================================================
-- RetailOS
-- V9 - Add Customers, Invoices, Invoice Items and Payments Tables
-- ==========================================================
CREATE TABLE customers (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    current_balance NUMERIC(12,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_customers_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id),
    -- Phone is optional; when present it must be unique within the shop
    -- (Postgres unique constraints ignore NULLs).
    CONSTRAINT uq_customers_shop_phone UNIQUE (shop_id, phone)
);

CREATE INDEX idx_customers_shop
    ON customers (shop_id);


CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    customer_id UUID,
    invoice_number VARCHAR(50) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_invoices_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_invoices_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT uq_invoices_shop_number UNIQUE (shop_id, invoice_number),
    CONSTRAINT chk_invoices_status
        CHECK (status IN ('DRAFT', 'HELD', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_invoices_total_amount
        CHECK (total_amount >= 0)
);

CREATE INDEX idx_invoices_shop
    ON invoices (shop_id);

CREATE INDEX idx_invoices_customer
    ON invoices (customer_id);


CREATE TABLE invoice_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL,
    -- Nullable so billing history survives product deletion.
    product_id UUID,
    quantity NUMERIC(12,3) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_invoice_items_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_items_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE SET NULL,
    CONSTRAINT chk_invoice_items_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_invoice_items_line_total
        CHECK (line_total >= 0)
);

CREATE INDEX idx_invoice_items_invoice
    ON invoice_items (invoice_id);

CREATE INDEX idx_invoice_items_product
    ON invoice_items (product_id);


CREATE TABLE payments (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    invoice_id UUID,
    customer_id UUID,
    amount NUMERIC(12,2) NOT NULL,
    payment_mode VARCHAR(6) NOT NULL DEFAULT 'CASH',
    payment_date DATE NOT NULL DEFAULT CURRENT_DATE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_payments_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_payments_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices (id) ON DELETE SET NULL,
    CONSTRAINT fk_payments_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE SET NULL,
    CONSTRAINT chk_payments_amount
        CHECK (amount > 0),
    CONSTRAINT chk_payments_mode
        CHECK (payment_mode IN ('CASH', 'UPI', 'CARD', 'CREDIT'))
);

CREATE INDEX idx_payments_shop
    ON payments (shop_id);

CREATE INDEX idx_payments_invoice
    ON payments (invoice_id);

CREATE INDEX idx_payments_customer
    ON payments (customer_id);
