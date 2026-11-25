-- Decision 7B: customers may cancel an order before fulfillment; its stock is restored.
-- Statuses: PLACED (default), SHIPPED (fulfillment started, no longer cancellable), CANCELLED.
ALTER TABLE sales
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PLACED' AFTER user_id,
    ADD COLUMN cancelled_at DATETIME NULL AFTER status,
    ADD CONSTRAINT sales_status_valid CHECK (status IN ('PLACED', 'SHIPPED', 'CANCELLED'));

DROP PROCEDURE IF EXISTS cancel_sale;

DELIMITER $$
CREATE PROCEDURE cancel_sale(IN selected_sale_id INT, IN requesting_user_id INT)
BEGIN
    DECLARE sale_owner INT DEFAULT NULL;
    DECLARE sale_status VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT user_id, status INTO sale_owner, sale_status FROM sales WHERE id = selected_sale_id FOR UPDATE;
    -- An order owned by someone else looks the same as a missing one so IDs cannot be probed.
    IF sale_owner IS NULL OR sale_owner <> requesting_user_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The order was not found.';
    END IF;
    IF sale_status <> 'PLACED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Only orders that have not shipped can be cancelled.';
    END IF;

    UPDATE products p JOIN sale_lines l ON l.product_id = p.id
    SET p.quantity = p.quantity + l.quantity
    WHERE l.sale_id = selected_sale_id;
    UPDATE sales SET status = 'CANCELLED', cancelled_at = NOW() WHERE id = selected_sale_id;
    COMMIT;
END$$
DELIMITER ;
