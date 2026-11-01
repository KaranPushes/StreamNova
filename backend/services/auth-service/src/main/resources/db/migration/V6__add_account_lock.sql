ALTER TABLE IF EXISTS auth_users
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS auth_users
    ADD COLUMN account_locked_until TIMESTAMP NULL;