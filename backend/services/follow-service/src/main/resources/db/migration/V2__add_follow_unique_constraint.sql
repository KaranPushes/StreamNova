ALTER TABLE IF EXISTS follows
    ADD CONSTRAINT uk_follow_user_channel
        UNIQUE (user_id, channel_id);