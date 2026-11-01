ALTER TABLE streams
    ADD COLUMN stream_key VARCHAR(128);

CREATE UNIQUE INDEX uk_streams_stream_key
    ON streams(stream_key);