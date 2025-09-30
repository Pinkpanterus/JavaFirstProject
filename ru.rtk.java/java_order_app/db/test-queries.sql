-- 1. Заказы за последние 7 дней
SELECT o.id, c.first_name, c.last_name, p.name, o.quantity, o.order_date
FROM "order" o
JOIN customer c ON o.customer_id = c.id
JOIN product p ON o.product_id = p.id
WHERE o.order_date >= CURRENT_DATE - INTERVAL '7 days';

-- 2. Топ-3 популярных товара
SELECT p.name, SUM(o.quantity) as total_sold
FROM "order" o
JOIN product p ON o.product_id = p.id
GROUP BY p.id
ORDER BY total_sold DESC
LIMIT 3;

-- 3. Обновить количество товара при продаже
UPDATE product SET quantity = quantity - 2 WHERE id = 1;

-- 4. Удалить клиентов без заказов
DELETE FROM customer WHERE id NOT IN (SELECT DISTINCT customer_id FROM "order");

-- 1. Чтение: последние 7 дней
SELECT o.id, c.first_name, p.name, o.quantity, o.order_date
FROM "order" o
JOIN customer c ON o.customer_id = c.id
JOIN product p ON o.product_id = p.id
WHERE o.order_date >= CURRENT_DATE - INTERVAL '7 days';

-- 2. Топ-3 товара по кол-ву продаж
SELECT p.name, SUM(o.quantity) AS sold
FROM "order" o
JOIN product p ON o.product_id = p.id
GROUP BY p.id
ORDER BY sold DESC
LIMIT 3;

-- 3. Общая выручка за текущий месяц
SELECT SUM(p.price * o.quantity) AS revenue
FROM "order" o
JOIN product p ON o.product_id = p.id
WHERE date_trunc('month', o.order_date) = date_trunc('month', CURRENT_DATE);

-- 4. Клиенты без заказов
SELECT c.*
FROM customer c
WHERE NOT EXISTS (SELECT 1 FROM "order" o WHERE o.customer_id = c.id);

-- 5. Кол-во заказов по статусам
SELECT os.name, COUNT(*) AS cnt
FROM "order" o
JOIN order_status os ON o.status_id = os.id
GROUP BY os.name;

-- 6. UPDATE: увеличить цену на 10 % для категории Electronics
UPDATE product
SET price = price * 1.10
WHERE category = 'Electronics';

-- 7. UPDATE: установить статус 'COMPLETED' заказам старше 30 дней
UPDATE "order"
SET status_id = (SELECT id FROM order_status WHERE name = 'COMPLETED')
WHERE order_date < CURRENT_DATE - INTERVAL '30 days';

-- 8. UPDATE: уменьшить кол-во товара на складе после продажи
UPDATE product
SET quantity = quantity - 2
WHERE id = 1;

-- 9. DELETE: удалить клиентов без заказов
DELETE FROM customer
WHERE id NOT IN (SELECT DISTINCT customer_id FROM "order");

--10. DELETE: удалить товары, которые никогда не заказывали
DELETE FROM product
WHERE id NOT IN (SELECT DISTINCT product_id FROM "order");