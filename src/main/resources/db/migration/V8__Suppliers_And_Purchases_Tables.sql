-- ==========================================================
-- RetailOS
-- V8 - Add Suppliers, Purchases and Purchase Items Tables
-- ==========================================================
CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_suppliers_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id)
);

-- Backs the case-insensitive duplicate check in SupplierService.
CREATE UNIQUE INDEX uq_suppliers_shop_name
    ON suppliers (shop_id, LOWER(name));

CREATE INDEX idx_suppliers_shop
    ON suppliers (shop_id);


CREATE TABLE purchases (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    supplier_id UUID,
    purchase_date DATE NOT NULL DEFAULT CURRENT_DATE,
    total_amount NUMERIC(12,2) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_purchases_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_purchases_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE SET NULL,
    CONSTRAINT chk_purchases_total_amount
        CHECK (total_amount >= 0)
);

CREATE INDEX idx_purchases_shop
    ON purchases (shop_id);

CREATE INDEX idx_purchases_supplier
    ON purchases (supplier_id);


CREATE TABLE purchase_items (
    id UUID PRIMARY KEY,
    purchase_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    purchase_price NUMERIC(10,2) NOT NULL,
    subtotal NUMERIC(12,2) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_purchase_items_purchase
        FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_items_product
        FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT chk_purchase_items_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_purchase_items_purchase_price
        CHECK (purchase_price >= 0),
    CONSTRAINT chk_purchase_items_subtotal
        CHECK (subtotal >= 0)
);

CREATE INDEX idx_purchase_items_purchase
    ON purchase_items (purchase_id);

CREATE INDEX idx_purchase_items_product
    ON purchase_items (product_id);
