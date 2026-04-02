-- ==================== Insert Roles ====================
-- Password: 'password123' (hashed with BCrypt)
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Administrator with full system access'),
    ('USER', 'Regular user with limited access');

-- ==================== Insert Users ====================
-- Password: 'admin123' -> $2a$10$slYQmyNdGzin7olVN3p5aOSvzxiXXeVQq8S2qDKlSn0pF3HnnXHWa
-- Password: 'user123' -> $2a$10$slYQmyNdGzin7olVN3p5aOSvzxiXXeVQq8S2qDKlSn0pF3HnnXHWa
INSERT INTO users (email, password, full_name, phone, address, is_active, role_id) VALUES
    ('admin@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvzxiXXeVQq8S2qDKlSn0pF3HnnXHWa', 'Admin User', '0123456789', '123 Admin Street, Admin City', TRUE, 1),
    ('john@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvzxiXXeVQq8S2qDKlSn0pF3HnnXHWa', 'John Doe', '0987654321', '456 User Avenue, User Town', TRUE, 2),
    ('jane@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvzxiXXeVQq8S2qDKlSn0pF3HnnXHWa', 'Jane Smith', '0912345678', '789 Customer Boulevard, Shopper City', TRUE, 2),
    ('michael@example.com', '$2a$10$slYQmyNdGzin7olVN3p5aOSvzxiXXeVQq8S2qDKlSn0pF3HnnXHWa', 'Michael Johnson', '0934567890', '321 Buyer Lane, Purchase Town', TRUE, 2);

-- ==================== Insert Categories ====================
INSERT INTO categories (name, description, is_active) VALUES
    ('Electronics', 'Electronic devices, gadgets, and accessories', TRUE),
    ('Clothing', 'Apparel, fashion items, and accessories', TRUE),
    ('Books', 'Educational, technical, and entertainment books', TRUE),
    ('Home & Garden', 'Home improvement and gardening products', TRUE),
    ('Sports & Outdoors', 'Sports equipment and outdoor gear', TRUE);

-- ==================== Insert Products ====================
INSERT INTO products (name, description, price, stock, category_id, is_active) VALUES
    -- Electronics
    ('Laptop Pro 15', 'High-performance laptop with Intel i9 and 16GB RAM', 1299.99, 50, 1, TRUE),
    ('Wireless Mouse', 'Ergonomic wireless mouse with 2.4GHz receiver', 29.99, 200, 1, TRUE),
    ('USB-C Cable 2m', 'High-speed USB-C cable for charging and data transfer', 9.99, 500, 1, TRUE),
    ('Mechanical Keyboard', 'RGB mechanical keyboard with Cherry MX switches', 129.99, 75, 1, TRUE),
    ('4K Monitor 32"', 'Ultra HD 4K monitor with 60Hz refresh rate', 599.99, 30, 1, TRUE),

    -- Clothing
    ('Classic Cotton T-Shirt', 'Comfortable 100% cotton t-shirt in various colors', 19.99, 150, 2, TRUE),
    ('Slim Fit Jeans', 'Premium denim jeans with stretch fabric', 79.99, 100, 2, TRUE),
    ('Winter Jacket', 'Waterproof winter jacket with thermal lining', 149.99, 45, 2, TRUE),
    ('Sports Sneakers', 'Breathable athletic sneakers with cushioning', 89.99, 120, 2, TRUE),
    ('Casual Hoodie', 'Comfortable fleece hoodie in multiple colors', 44.99, 80, 2, TRUE),

    -- Books
    ('Python Programming Guide', 'Complete guide to learning Python from scratch', 39.99, 75, 3, TRUE),
    ('Spring Boot in Action', 'Master Spring Boot development and microservices', 49.99, 60, 3, TRUE),
    ('Clean Code', 'A Handbook of Agile Software Craftsmanship', 59.99, 55, 3, TRUE),
    ('Design Patterns', 'Elements of Reusable Object-Oriented Software', 54.99, 40, 3, TRUE),

    -- Home & Garden
    ('Professional Garden Tools Set', 'Complete 12-piece gardening toolkit', 89.99, 40, 4, TRUE),
    ('LED Desk Lamp', 'Adjustable LED desk lamp with USB charging', 34.99, 120, 4, TRUE),
    ('Bamboo Cutting Board Set', 'Set of 3 bamboo cutting boards with handles', 24.99, 100, 4, TRUE),
    ('Plant Pot Set', 'Set of 5 terracotta plant pots with drainage', 19.99, 80, 4, TRUE),

    -- Sports & Outdoors
    ('Mountain Bike', 'Full-suspension mountain bike with 21 gears', 499.99, 25, 5, TRUE),
    ('Yoga Mat Premium', 'Non-slip TPE yoga mat with carrying strap', 29.99, 150, 5, TRUE),
    ('Camping Tent 4-Person', 'Waterproof tent suitable for all weather', 199.99, 35, 5, TRUE),
    ('Running Shoes', 'Lightweight running shoes with cushioned sole', 99.99, 90, 5, TRUE);

-- ==================== Insert Carts for Users ====================
INSERT INTO carts (user_id) VALUES (2), (3), (4);

-- ==================== Insert Sample Orders ====================
INSERT INTO orders (user_id, total_amount, status, shipping_address, is_deleted) VALUES
    (2, 1329.98, 'DELIVERED', '456 User Avenue, User Town', FALSE),
    (3, 299.98, 'SHIPPED', '789 Customer Boulevard, Shopper City', FALSE),
    (4, 199.98, 'PENDING', '321 Buyer Lane, Purchase Town', FALSE);

-- ==================== Insert Order Items ====================
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
    -- Order 1: Laptop + Mouse
    (1, 1, 1, 1299.99),
    (1, 2, 1, 29.99),
    -- Order 2: T-Shirt + Jeans
    (2, 6, 2, 19.99),
    (2, 7, 1, 79.99),
    -- Order 3: Python Book + Spring Boot Book
    (3, 11, 1, 39.99),
    (3, 12, 1, 49.99);

-- ==================== Set Sequences ====================
-- Ensure sequences start after the highest ID
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles) + 1);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users) + 1);
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories) + 1);
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products) + 1);
SELECT setval('carts_id_seq', (SELECT MAX(id) FROM carts) + 1);
SELECT setval('cart_items_id_seq', (SELECT MAX(id) FROM cart_items) + 1);
SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders) + 1);
SELECT setval('order_items_id_seq', (SELECT MAX(id) FROM order_items) + 1);
