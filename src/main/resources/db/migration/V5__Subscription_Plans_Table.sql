-- ==========================================================
-- RetailOS
-- V5 - Add Subscription Plans Table
-- ==========================================================
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    billing_cycle VARCHAR(10) NOT NULL DEFAULT 'ANNUAL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    CONSTRAINT chk_subscription_plans_billing_cycle
        CHECK (billing_cycle IN ('MONTHLY', 'ANNUAL')),
    CONSTRAINT chk_subscription_plans_price
        CHECK (price >= 0)
);

-- Backs the case-insensitive duplicate check in SubscriptionPlanService
-- (same plan name may exist once per billing cycle).
CREATE UNIQUE INDEX uq_subscription_plans_name_cycle
    ON subscription_plans (LOWER(name), billing_cycle);
