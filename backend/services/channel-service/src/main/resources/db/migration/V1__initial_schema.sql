CREATE TABLE channels (
  id UUID PRIMARY KEY,

  owner_user_id UUID NOT NULL,

  slug VARCHAR(50) NOT NULL,

  display_name VARCHAR(100) NOT NULL,

  description VARCHAR(1000),

  avatar_url VARCHAR(2048),

  banner_url VARCHAR(2048),

  status VARCHAR(30) NOT NULL,

  created_at TIMESTAMP WITH TIME ZONE NOT NULL,

  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

  CONSTRAINT uk_channels_owner_user_id
      UNIQUE (owner_user_id),

  CONSTRAINT uk_channels_slug
      UNIQUE (slug),

  CONSTRAINT chk_channels_status
      CHECK (
          status IN (
                     'ACTIVE',
                     'SUSPENDED',
                     'BANNED',
                     'DEACTIVATED'
              )
          )
);

CREATE INDEX idx_channels_status
    ON channels(status);