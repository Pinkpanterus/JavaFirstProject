-- Справочник статусов заказов
create TABLE IF NOT EXISTS order_status (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица товаров
create TABLE IF NOT EXISTS product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) CHECK (price >= 0),
    quantity INTEGER CHECK (quantity >= 0),
    category VARCHAR(50)
);

-- Таблица клиентов
create TABLE IF NOT EXISTS customer (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE
);

-- Таблица заказов
create TABLE IF NOT EXISTS "order" (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES product(id),
    customer_id INTEGER REFERENCES customer(id),
    order_date DATE DEFAULT CURRENT_DATE,
    quantity INTEGER CHECK (quantity > 0),
    status_id INTEGER REFERENCES order_status(id)
);

-- Индексы
create index idx_order_date on "order"(order_date);
create index idx_order_product on "order"(product_id);
create index idx_order_customer on "order"(customer_id);