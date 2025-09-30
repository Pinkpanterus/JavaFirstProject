INSERT INTO order_status (name) VALUES ('ARCHIVED');   -- всего 5

INSERT INTO product (name, description, price, quantity, category) VALUES
('MacBook Pro 16', 'Apple laptop M3', 2499.00, 5, 'Electronics'),
('iPad Air', 'Tablet 10,9"', 799.00, 12, 'Electronics'),
('Sony WH-1000XM5', 'Noise-cancelling headphones', 399.00, 20, 'Electronics'),
('Xiaomi Mi 13', 'Flagship Android', 899.00, 8, 'Electronics'),
('Dell XPS 13', 'Ultrabook', 1199.00, 6, 'Electronics'),
('LG OLED 55', 'Smart TV 4K', 1499.00, 3, 'Electronics'),
('PlayStation 5', 'Game console', 499.00, 7, 'Electronics'),
('AirPods Pro 2', 'Wireless earbuds', 249.00, 15, 'Electronics');

INSERT INTO customer (first_name, last_name, phone, email) VALUES
('Anna', 'Petrova', '+79992345678', 'anna@mail.com'),
('Petr', 'Sidorov', '+79993456789', 'petr@mail.com'),
('Olga', 'Kuznetsova', '+79994567890', 'olga@mail.com'),
('Dmitry', 'Morozov', '+79995678901', 'dmitry@mail.com'),
('Elena', 'Volkova', '+79996789012', 'elena@mail.com'),
('Sergey', 'Alekseev', '+79997890123', 'sergey@mail.com'),
('Maria', 'Belova', '+79998901234', 'maria@mail.com'),
('Alex', 'Andreev', '+79999012345', 'alex@mail.com'),
('Natalia', 'Gromova', '+79990123456', 'natalia@mail.com');

INSERT INTO "order" (product_id, customer_id, order_date, quantity, status_id) VALUES
(2, 2, CURRENT_DATE - INTERVAL '1 day', 1, 2),
(3, 3, CURRENT_DATE - INTERVAL '2 days', 2, 3),
(4, 4, CURRENT_DATE - INTERVAL '3 days', 1, 1),
(5, 5, CURRENT_DATE - INTERVAL '4 days', 3, 2),
(6, 6, CURRENT_DATE - INTERVAL '5 days', 1, 3),
(7, 7, CURRENT_DATE - INTERVAL '6 days', 1, 1),
(8, 8, CURRENT_DATE - INTERVAL '7 days', 2, 2),
(9, 9, CURRENT_DATE - INTERVAL '8 days', 1, 3),
(10, 10, CURRENT_DATE - INTERVAL '9 days', 1, 1);