CREATE TABLE users (
   id UUID NOT NULL,

   username VARCHAR(30) NOT NULL,
   display_name VARCHAR(50) NOT NULL,
   email VARCHAR(255) NOT NULL,
   status VARCHAR(20) NOT NULL,

   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
   updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

   CONSTRAINT pk_users PRIMARY KEY (id),
   CONSTRAINT uk_users_username UNIQUE (username),
   CONSTRAINT uk_users_email UNIQUE (email),
   CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'PENDING_DELETION'))
);