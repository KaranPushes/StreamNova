CREATE TABLE IF NOT EXISTS chat_moderations (
  id UUID PRIMARY KEY,
  stream_id UUID NOT NULL,
  user_id UUID NOT NULL,
  type VARCHAR(20) NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_chat_moderation_stream_user
    ON chat_moderations (stream_id, user_id);
