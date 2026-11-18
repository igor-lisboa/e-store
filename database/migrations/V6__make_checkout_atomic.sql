-- Keep earlier migrations immutable; replace the legacy routine with a transaction boundary.
DROP PROCEDURE IF EXISTS buy_cart_itens;

DELIMITER $$
CREATE PROCEDURE checkout_cart(IN selected_cart_id INT, IN selected_address_id INT)
BEGIN
    DECLARE cart_owner INT DEFAULT NULL;
    DECLARE selected_product_id INT;
    DECLARE requested_quantity INT;
    DECLARE unit_price DECIMAL(11,2);
    DECLARE order_total DECIMAL(20,2) DEFAULT 0;
    DECLARE item_count INT DEFAULT 0;
    DECLARE finished BOOLEAN DEFAULT FALSE;
    DECLARE cart_items CURSOR FOR
        SELECT product_id, quantity FROM carts_products
        WHERE cart_id = selected_cart_id ORDER BY product_id FOR UPDATE;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = TRUE;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
    SELECT user_id INTO cart_owner FROM carts WHERE id = selected_cart_id FOR UPDATE;
    IF cart_owner IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Checkout requires an account-owned cart.';
    END IF;
    IF EXISTS (SELECT 1 FROM sales WHERE cart_id = selected_cart_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This cart has already been purchased.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM address WHERE id = selected_address_id AND user_id = cart_owner) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The address does not belong to the cart owner.';
    END IF;

    OPEN cart_items;
    read_items: LOOP
        FETCH cart_items INTO selected_product_id, requested_quantity;
        IF finished THEN
            LEAVE read_items;
        END IF;
        IF requested_quantity <= 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cart quantities must be positive.';
        END IF;
        UPDATE products SET quantity = quantity - requested_quantity
        WHERE id = selected_product_id AND quantity >= requested_quantity;
        IF ROW_COUNT() <> 1 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Requested quantity exceeds available stock.';
        END IF;
        SELECT price INTO unit_price FROM products WHERE id = selected_product_id;
        SET order_total = order_total + unit_price * requested_quantity;
        SET item_count = item_count + 1;
    END LOOP;
    CLOSE cart_items;

    IF item_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An empty cart cannot be purchased.';
    END IF;
    INSERT INTO sales (cart_id, total_price, created_at, address_id, user_id)
    VALUES (selected_cart_id, order_total, NOW(), selected_address_id, cart_owner);
    COMMIT;
END$$
DELIMITER ;
