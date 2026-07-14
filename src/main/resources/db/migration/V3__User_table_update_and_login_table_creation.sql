-- ==========================================================
-- RetailOS
-- V3 - Add version column in User Table
--    - Add RefreshToken Table
-- ==========================================================
ALTER TABLE users
ALTER COLUMN active
SET DEFAULT TRUE;

ALTER TABLE users
ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),

    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    user_id UUID NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,

    created_by UUID,
    updated_by UUID,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


CREATE INDEX idx_refresh_tokens_user_id
ON refresh_tokens(user_id);
