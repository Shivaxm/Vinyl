SELECT setval('products_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products));
