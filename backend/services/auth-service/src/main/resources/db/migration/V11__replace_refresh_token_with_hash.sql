DELETE FROM user_sessions;

ALTER TABLE user_sessions
    DROP COLUMN refresh_token;

ALTER TABLE user_sessions
    ADD COLUMN refresh_token_hash VARCHAR(64) NOT NULL;

ALTER TABLE user_sessions
    ADD CONSTRAINT uk_user_sessions_refresh_token_hash
        UNIQUE (refresh_token_hash);