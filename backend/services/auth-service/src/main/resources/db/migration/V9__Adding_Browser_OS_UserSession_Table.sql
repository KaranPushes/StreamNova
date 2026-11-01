ALTER TABLE IF EXISTS user_sessions
    ADD COLUMN browser VARCHAR(50);

ALTER TABLE IF EXISTS user_sessions
    ADD COLUMN operating_system VARCHAR(50);