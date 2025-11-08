-- carts table
CREATE TABLE carts (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       date_created DATE NOT NULL DEFAULT CURRENT_DATE
);

-- cart_items table
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            cart_id UUID NOT NULL,
                            product_id BIGINT NOT NULL,
                            quantity INT NOT NULL DEFAULT 1,
                            CONSTRAINT uq_cart_items UNIQUE (cart_id, product_id),
                            CONSTRAINT fk_cart_items_cart
                                FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                            CONSTRAINT fk_cart_items_product
                                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- optional indexes for query performance
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
