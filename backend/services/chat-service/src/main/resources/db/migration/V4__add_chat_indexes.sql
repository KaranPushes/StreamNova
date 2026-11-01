CREATE INDEX IF NOT EXISTS idx_chat_messages_stream_user
    ON chat_messages (stream_id, user_id);

CREATE INDEX IF NOT EXISTS idx_chat_messages_stream_deleted
    ON chat_messages (stream_id, deleted);

CREATE INDEX IF NOT EXISTS idx_chat_moderations_stream_user_type
    ON chat_moderations (stream_id, user_id, type);