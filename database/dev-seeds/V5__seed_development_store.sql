-- Local development accounts only. Never enable these fixtures in production.
INSERT INTO users (id, name, email, password, role_id) VALUES
(1, 'Development Admin', 'admin@example.test', '$pbkdf2-sha256$600000$ZS1zdG9yZS1kZXYtc2FsdCE$AEy9ZOM7AY-WQjjyAB1M_n1hMAQuA9KfE3Os_tIKfbE', 1),
(2, 'Development Customer', 'customer@example.test', '$pbkdf2-sha256$600000$ZS1zdG9yZS1kZXYtc2FsdCE$AEy9ZOM7AY-WQjjyAB1M_n1hMAQuA9KfE3Os_tIKfbE', 2);
INSERT INTO products (name, price, description, img, category_id, quantity, created_at) VALUES
('PlayStation Console', 499.00, 'Development console', 'img/ps4.jpg', 5, 100, NOW()),
('Xbox Console', 399.00, 'Development console', 'img/xone.jpg', 8, 100, NOW()),
('Wii Console', 199.00, 'Development console', 'img/wii.jpg', 11, 100, NOW());
