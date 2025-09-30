INSERT INTO order_status (name) VALUES
('NEW'), ('PROCESSING'), ('COMPLETED'), ('CANCELLED');

INSERT INTO product (name, description, price, quantity, category) VALUES
('iPhone 15', 'Latest Apple smartphone', 1200.00, 10, 'Electronics'),
('Samsung Galaxy S23', 'Android flagship', 1000.00, 15, 'Electronics');

INSERT INTO customer (first_name, last_name, phone, email) VALUES
('Ivan', 'Ivanov', '+79991234567', 'ivan@mail.com');

INSERT INTO "order" (product_id, customer_id, order_date, quantity, status_id) VALUES
(1, 1, CURRENT_DATE, 2, 1);