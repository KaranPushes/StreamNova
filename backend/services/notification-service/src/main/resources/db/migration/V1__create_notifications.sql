CREATE TABLE notifications (
   id UUID PRIMARY KEY,
   user_id UUID NOT NULL,
   type VARCHAR(30) NOT NULL,
   title VARCHAR(150) NOT NULL,
   message VARCHAR(500) NOT NULL,
   reference_id UUID,
   read BOOLEAN NOT NULL DEFAULT FALSE,
   created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notification_user_created
    ON notifications (user_id, created_at);

CREATE INDEX idx_notification_user_read
    ON notifications (user_id, read);