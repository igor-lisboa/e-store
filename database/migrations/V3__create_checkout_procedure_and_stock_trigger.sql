DELIMITER ;;
CREATE PROCEDURE `buy_cart_itens`(IN f_cart_id INT (11),
	IN f_address_id INT (11))
BEGIN
	DECLARE
		f_user_id,
		fp_qtd_cart_itens,
		fp_cur_cart_item,
		fp_product_id,
		fp_quantity INT DEFAULT 0;

DECLARE
	f_total_price DECIMAL(11,2) DEFAULT 0;

SELECT
	COUNT(*) INTO fp_qtd_cart_itens
FROM
	carts_products
WHERE
	cart_id = f_cart_id
ORDER BY
	1;


WHILE fp_cur_cart_item < fp_qtd_cart_itens DO
	SELECT
		product_id,
		quantity INTO fp_product_id,
		fp_quantity
	FROM
		carts_products
	WHERE
		cart_id = f_cart_id
	ORDER BY
		1
	LIMIT 1 OFFSET fp_cur_cart_item;

UPDATE products
SET quantity = quantity - fp_quantity
WHERE
	id = fp_product_id;

SELECT
	fp_cur_cart_item + 1 INTO fp_cur_cart_item;


END
WHILE;

SELECT
	sum(p.price * c.quantity),
	ca.user_id INTO f_total_price,
	f_user_id
FROM
	carts_products c,
	products p,
	carts ca
WHERE
	c.cart_id = f_cart_id
AND c.product_id = p.id
AND c.cart_id = ca.id
GROUP BY
	c.cart_id
LIMIT 1;

INSERT INTO `sales` (
	cart_id,
	total_price,
	created_at,
	address_id,
	user_id
)
VALUES
	(
		f_cart_id,
		f_total_price,
		sysdate(),
		f_address_id,
		f_user_id
	);

END
;;
DELIMITER ;
DROP TRIGGER IF EXISTS `check_quantity`;
DELIMITER ;;
CREATE TRIGGER `check_quantity` AFTER UPDATE ON `products` FOR EACH ROW BEGIN

IF NEW.quantity < 0 THEN
	SIGNAL SQLSTATE '45000'
SET MESSAGE_TEXT = 'Stock quantity cannot be negative.';


END IF;
END
;;
DELIMITER ;
