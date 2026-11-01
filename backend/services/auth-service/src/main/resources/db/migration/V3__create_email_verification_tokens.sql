CREATE TABLE IF NOT EXISTS email_verification_tokens
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    token VARCHAR(128) NOT NULL UNIQUE,

    expires_at TIMESTAMP NOT NULL,

    verified BOOLEAN NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL
);