CREATE TABLE books (
                       id            BIGSERIAL PRIMARY KEY,
                       title         VARCHAR(512) NOT NULL,
                       author        VARCHAR(512) NOT NULL,
                       publish_year  INT,
                       genre         VARCHAR(128),
                       summary       TEXT,
                       created_at    TIMESTAMPTZ DEFAULT now(),
                       updated_at    TIMESTAMPTZ DEFAULT now()
);

CREATE UNIQUE INDEX ux_book_unique ON books (title, author, publish_year);
