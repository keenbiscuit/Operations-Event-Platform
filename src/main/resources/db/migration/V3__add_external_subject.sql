ALTER TABLE users
    ADD COLUMN external_subject VARCHAR(255);

CREATE UNIQUE INDEX uq_users_external_subject
    ON users (external_subject);
