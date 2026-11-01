CREATE TABLE IF NOT EXISTS user_sessions
(
    id               UUID PRIMARY KEY,

    user_id          UUID                     NOT NULL,

    refresh_token    VARCHAR(512) UNIQUE      NOT NULL,

    expires_at       TIMESTAMP                NOT NULL,

    last_accessed_at TIMESTAMP                NOT NULL,

    revoked          BOOLEAN                  NOT NULL,

    device_name      VARCHAR(100),

    user_agent       VARCHAR(500),

    ip_address       VARCHAR(45),

    created_at       TIMESTAMP                NOT NULL,

    updated_at       TIMESTAMP                NOT NULL
);