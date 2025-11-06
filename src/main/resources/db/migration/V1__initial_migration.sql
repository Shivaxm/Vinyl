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


