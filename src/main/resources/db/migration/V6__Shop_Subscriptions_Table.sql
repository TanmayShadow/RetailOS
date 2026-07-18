-- ==========================================================
-- RetailOS
-- V6 - Add Shop Subscriptions Table
-- ==========================================================
CREATE TABLE shop_subscriptions (
    id UUID PRIMARY KEY,
    shop_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_shop_subscriptions_shop
        FOREIGN KEY (shop_id) REFERENCES shops (id),
    CONSTRAINT fk_shop_subscriptions_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans (id),
    CONSTRAINT chk_shop_subscriptions_status
        CHECK (status IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'CANCELLED')),
    CONSTRAINT chk_shop_subscriptions_dates
        CHECK (end_date >= start_date)
);

CREATE INDEX idx_shop_subscriptions_shop
    ON shop_subscriptions (shop_id);

CREATE INDEX idx_shop_subscriptions_plan
    ON shop_subscriptions (plan_id);
