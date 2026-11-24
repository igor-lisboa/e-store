-- Records when each cart was last used so anonymous carts can expire after inactivity.
ALTER TABLE carts ADD COLUMN last_activity_at DATETIME NULL AFTER created_at;

UPDATE carts SET last_activity_at = created_at;

ALTER TABLE carts MODIFY last_activity_at DATETIME NOT NULL;
