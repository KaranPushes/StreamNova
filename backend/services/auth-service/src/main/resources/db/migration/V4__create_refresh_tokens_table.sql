CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    token VARCHAR(255) NOT NULL UNIQUE,

    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user
    ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_token_token
    ON refresh_tokens(token);