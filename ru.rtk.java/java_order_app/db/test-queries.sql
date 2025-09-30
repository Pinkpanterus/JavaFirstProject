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