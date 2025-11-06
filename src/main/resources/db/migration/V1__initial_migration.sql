CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
                       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       email VARCHAR(250) NOT NULL UNIQUE,
                       password_hash VARCHAR(250) NOT NULL,
                       role VARCHAR(20) NOT NULL
                           CHECK (role IN ('CUSTOMER','ADMIN')),
                       status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','LOCKED','PENDING_VERIFICATION')),
                       created_at timestamptz DEFAULT now(),
                       updated_at timestamptz DEFAULT now()
);

CREATE TABLE products (
                          id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                          name TEXT NOT NULL,
                          slug TEXT NOT NULL UNIQUE,
                          description TEXT,
                          price_cents BIGINT NOT NULL CHECK (price_cents >= 0),
                          currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                          sku TEXT NOT NULL UNIQUE,
                          inventory_qty INT NOT NULL DEFAULT 0 CHECK (inventory_qty >= 0),
                          is_active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at timestamptz DEFAULT now(),
                          updated_at timestamptz DEFAULT now()
);

CREATE TABLE carts (
                       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                       version BIGINT NOT NULL DEFAULT 0,
                       created_at timestamptz DEFAULT now(),
                       updated_at timestamptz DEFAULT now()
);

CREATE TABLE cart_items (
                            id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                            cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
                            product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
                            quantity INT NOT NULL CHECK (quantity >= 1),
                            created_at timestamptz DEFAULT now(),
                            updated_at timestamptz DEFAULT now(),
                            CONSTRAINT uq_cartitem_cart_product UNIQUE (cart_id, product_id)
);




