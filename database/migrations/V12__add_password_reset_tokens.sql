-- Decision 4A: account recovery by email. Only a SHA-256 hash of each one-time token is stored, so a database
-- leak cannot be used to reset accounts. used_at marks a token as spent; expiry is checked in UTC.
CREATE TABLE password_reset_tokens (
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY password_reset_token_unique (token_hash),
    KEY password_reset_user_index (user_id),
    CONSTRAINT password_reset_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
