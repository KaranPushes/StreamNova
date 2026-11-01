-- Remove the old refresh-token table after migrating to UserSession.
DROP TABLE IF EXISTS refresh_tokens;

-- Speeds up session lookup by user.
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id
    ON user_sessions (user_id);

-- Speeds up GET /auth/sessions and session revocation operations.
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_active
    ON user_sessions (user_id, revoked, last_accessed_at DESC);

-- Useful for future expired-session cleanup.
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at
    ON user_sessions (expires_at);

-- Useful for cleaning old revoked sessions.
CREATE INDEX IF NOT EXISTS idx_user_sessions_revoked
    ON user_sessions (revoked);