INSERT INTO products (id, name, price, description, category_id) VALUES
(1, 'Wireless Headphones', 79.99, 'Over-ear Bluetooth headphones with ANC',
 (SELECT id FROM categories WHERE name = 'Electronics')),
(2, 'USB-C Charger 65W', 34.99, 'Fast charger for phones and laptops',
 (SELECT id FROM categories WHERE name = 'Electronics')),
(3, 'Modern Java in Action', 44.95, 'Java, streams, lambdas, and more',
 (SELECT id FROM categories WHERE name = 'Books')),
(4, 'Cotton T-Shirt', 12.50, '100% cotton crew neck',
 (SELECT id FROM categories WHERE name = 'Clothing')),
(5, 'Non-stick Frying Pan', 24.99, 'Dishwasher-safe 10-inch skillet',
 (SELECT id FROM categories WHERE name = 'Home')),
(6, 'Building Blocks Set', 19.99, 'Creative construction toy set',
 (SELECT id FROM categories WHERE name = 'Toys'));
