CREATE TABLE streams (
     id UUID PRIMARY KEY,
     channel_id UUID NOT NULL,
     title VARCHAR(150) NOT NULL,
     description VARCHAR(2000),
     category VARCHAR(100),
     thumbnail_url VARCHAR(2048),
     status VARCHAR(30) NOT NULL,
     scheduled_at TIMESTAMP WITH TIME ZONE,
     started_at TIMESTAMP WITH TIME ZONE,
     ended_at TIMESTAMP WITH TIME ZONE,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

     CONSTRAINT chk_streams_status
         CHECK (status IN ('SCHEDULED', 'LIVE', 'ENDED'))
);

CREATE INDEX idx_streams_channel_id
    ON streams(channel_id);

CREATE INDEX idx_streams_status
    ON streams(status);

CREATE INDEX idx_streams_scheduled_at
    ON streams(scheduled_at);