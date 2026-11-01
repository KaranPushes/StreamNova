CREATE TABLE IF NOT EXISTS chat_messages (
   id UUID PRIMARY KEY,
   stream_id UUID NOT NULL,
   user_id UUID NOT NULL,
   message VARCHAR(500) NOT NULL,
   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
   deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_chat_stream_created
    ON chat_messages (stream_id, created_at);