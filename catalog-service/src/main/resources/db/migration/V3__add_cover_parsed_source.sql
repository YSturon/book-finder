ALTER TABLE books
    ADD COLUMN cover_url  varchar(1024),
    ADD COLUMN parsed_at  timestamp       NOT NULL DEFAULT now(),
    ADD COLUMN source     varchar(128)    NOT NULL DEFAULT 'unknown';