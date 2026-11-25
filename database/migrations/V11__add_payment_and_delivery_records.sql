-- Decision 6: provider-agnostic payment and delivery records. Nothing here names a vendor; each adapter
-- fills the same neutral columns. raw_response_ref points at the vendor payload stored elsewhere.
CREATE TABLE payments (
    id INT NOT NULL AUTO_INCREMENT,
    sale_id INT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    external_reference VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(20,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'BRL',
    raw_response_ref VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY payments_sale_unique (sale_id),
    CONSTRAINT payments_sale_fk FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE CASCADE,
    CONSTRAINT payments_status_valid CHECK (status IN ('NOT_CHARGED', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- The address columns are a snapshot: editing the customer's address later must not change past orders.
CREATE TABLE deliveries (
    id INT NOT NULL AUTO_INCREMENT,
    sale_id INT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    method VARCHAR(50) NOT NULL,
    tracking_code VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    postal_code INT NOT NULL,
    country VARCHAR(255) NOT NULL,
    raw_response_ref VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY deliveries_sale_unique (sale_id),
    CONSTRAINT deliveries_sale_fk FOREIGN KEY (sale_id) REFERENCES sales (id) ON DELETE CASCADE,
    CONSTRAINT deliveries_status_valid CHECK (status IN ('NOT_ARRANGED', 'PENDING', 'SHIPPED', 'DELIVERED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Earlier orders came from the demonstration checkout: nothing was charged and no delivery was arranged.
INSERT INTO payments (sale_id, provider, status, amount, created_at)
SELECT id, 'demo', 'NOT_CHARGED', total_price, created_at FROM sales;
INSERT INTO deliveries (sale_id, provider, method, status, street, city, state, postal_code, country, created_at)
SELECT s.id, 'demo', 'NONE', 'NOT_ARRANGED', a.address, a.city, a.state, a.zipcode, a.country, s.created_at
FROM sales s JOIN address a ON a.id = s.address_id;

DROP PROCEDURE IF EXISTS checkout_cart;

DELIMITER $$
CREATE PROCEDURE place_order(
    IN selected_cart_id INT, IN selected_address_id INT,
    IN payment_provider VARCHAR(50), IN payment_reference VARCHAR(100), IN payment_status VARCHAR(20),
    IN payment_raw_ref VARCHAR(255),
    IN delivery_provider VARCHAR(50), IN delivery_method VARCHAR(50), IN delivery_tracking VARCHAR(100),
    IN delivery_status VARCHAR(20), IN delivery_raw_ref VARCHAR(255))
BEGIN
    DECLARE cart_owner INT DEFAULT NULL;
    DECLARE selected_product_id INT;
    DECLARE requested_quantity INT;
    DECLARE unit_price DECIMAL(11,2);
    DECLARE order_total DECIMAL(20,2) DEFAULT 0;
    DECLARE item_count INT DEFAULT 0;
    DECLARE new_sale_id INT;
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
    SET new_sale_id = LAST_INSERT_ID();
    INSERT INTO sale_lines (sale_id, product_id, product_name, unit_price, quantity)
    SELECT new_sale_id, p.id, p.name, p.price, cp.quantity
    FROM carts_products cp JOIN products p ON p.id = cp.product_id
    WHERE cp.cart_id = selected_cart_id;
    INSERT INTO payments (sale_id, provider, external_reference, status, amount, raw_response_ref, created_at)
    VALUES (new_sale_id, payment_provider, payment_reference, payment_status, order_total, payment_raw_ref, NOW());
    INSERT INTO deliveries (sale_id, provider, method, tracking_code, status, street, city, state, postal_code,
                            country, raw_response_ref, created_at)
    SELECT new_sale_id, delivery_provider, delivery_method, delivery_tracking, delivery_status, a.address, a.city,
           a.state, a.zipcode, a.country, delivery_raw_ref, NOW()
    FROM address a WHERE a.id = selected_address_id;
    COMMIT;
END$$
DELIMITER ;

-- Callers that do not use a payment or delivery vendor keep the original two-argument entry point.
DELIMITER $$
CREATE PROCEDURE checkout_cart(IN selected_cart_id INT, IN selected_address_id INT)
BEGIN
    CALL place_order(selected_cart_id, selected_address_id, 'demo', NULL, 'NOT_CHARGED', NULL,
                     'demo', 'NONE', NULL, 'NOT_ARRANGED', NULL);
END$$
DELIMITER ;
