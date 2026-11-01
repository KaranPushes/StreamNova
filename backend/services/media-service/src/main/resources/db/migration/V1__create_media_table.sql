CREATE TABLE media (
   id UUID PRIMARY KEY,
   owner_user_id UUID NOT NULL,

   media_type VARCHAR(30) NOT NULL,

   original_file_name VARCHAR(255) NOT NULL,

   storage_key VARCHAR(500) NOT NULL UNIQUE,

   url VARCHAR(1000) NOT NULL,

   content_type VARCHAR(100) NOT NULL,

   file_size BIGINT NOT NULL,

   status VARCHAR(20) NOT NULL,

   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
   updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_media_owner_user_id
    ON media(owner_user_id);

CREATE INDEX idx_media_type
    ON media(media_type);

CREATE INDEX idx_media_status
    ON media(status);