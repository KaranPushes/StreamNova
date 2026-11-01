CREATE TABLE IF NOT EXISTS chat_moderators (
 id UUID PRIMARY KEY,
 stream_id UUID NOT NULL,
 user_id UUID NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL,
 CONSTRAINT uk_chat_moderator_stream_user
     UNIQUE (stream_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_moderator_stream_user
    ON chat_moderators (stream_id, user_id);