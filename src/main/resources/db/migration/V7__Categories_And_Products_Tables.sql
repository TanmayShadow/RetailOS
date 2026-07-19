-- ==========================================================
-- RetailOS
-- V7 - Add Categories and Products Tables
-- ==========================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_categories_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id)
);

-- Backs the case-insensitive duplicate check in CategoryService.
CREATE UNIQUE INDEX uq_categories_shop_name
    ON categories (shop_id, LOWER(name));

CREATE INDEX idx_categories_shop
    ON categories (shop_id);


CREATE TABLE products (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(150) NOT NULL,
    sku VARCHAR(50),
    selling_price NUMERIC(10,2) NOT NULL,
    current_stock NUMERIC(12,3) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_products_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL,
    CONSTRAINT chk_products_selling_price
        CHECK (selling_price >= 0)
);

-- SKU is optional; when present it must be unique within the shop
-- (case-insensitive). Postgres unique indexes ignore NULLs.
CREATE UNIQUE INDEX uq_products_shop_sku
    ON products (shop_id, LOWER(sku));

CREATE INDEX idx_products_shop
    ON products (shop_id);

CREATE INDEX idx_products_category
    ON products (category_id);
