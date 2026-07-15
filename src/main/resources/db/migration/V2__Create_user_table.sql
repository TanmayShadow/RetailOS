-- ==========================================================
-- RetailOS
-- V2 - Create Users Table
-- ==========================================================

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100),

    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,

    role            VARCHAR(50) NOT NULL,

    shop_id         UUID,

    active          BOOLEAN NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_by      UUID,
    updated_by      UUID,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_users_shop_id
ON users(shop_id);