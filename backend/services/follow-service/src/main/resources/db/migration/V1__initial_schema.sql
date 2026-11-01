CREATE TABLE follows (
 id UUID PRIMARY KEY,

 user_id UUID NOT NULL,
 channel_id UUID NOT NULL,

 created_at TIMESTAMP WITH TIME ZONE NOT NULL,

 CONSTRAINT uk_follows_user_channel
     UNIQUE (user_id, channel_id)
);

CREATE INDEX idx_follows_user_id
    ON follows(user_id);

CREATE INDEX idx_follows_channel_id
    ON follows(channel_id);