-- TABLES
CREATE TABLE users (
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL DEFAULT 'USER'
);

CREATE TABLE addresses (
                           id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           street  VARCHAR(255) NOT NULL,
                           city    VARCHAR(255) NOT NULL,
                           state   VARCHAR(255) NOT NULL,
                           zip     VARCHAR(255) NOT NULL,
                           user_id BIGINT NOT NULL
);

CREATE TABLE categories (
                            id   SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            name VARCHAR(255) NOT NULL
);

CREATE TABLE products (
                          id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          name          VARCHAR(255)   NOT NULL,
                          price         NUMERIC(10, 2) NOT NULL,
                          description   TEXT           NOT NULL,
                          category_id   SMALLINT
);

CREATE TABLE profiles (
                          id             BIGINT PRIMARY KEY,
                          bio            TEXT,
                          phone_number   VARCHAR(15),
                          date_of_birth  DATE,
                          loyalty_points INTEGER DEFAULT 0 CHECK (loyalty_points >= 0)
);

CREATE TABLE wishlist (
                          product_id BIGINT NOT NULL,
                          user_id    BIGINT NOT NULL,
                          PRIMARY KEY (product_id, user_id)
);

-- FOREIGN KEYS
ALTER TABLE addresses
    ADD CONSTRAINT addresses_users_id_fk
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE products
    ADD CONSTRAINT fk_category
        FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE NO ACTION;

ALTER TABLE wishlist
    ADD CONSTRAINT fk_wishlist_on_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE;

ALTER TABLE wishlist
    ADD CONSTRAINT fk_wishlist_on_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE NO ACTION;

ALTER TABLE profiles
    ADD CONSTRAINT profiles_ibfk_1
        FOREIGN KEY (id) REFERENCES users (id) ON DELETE NO ACTION;

-- INDEXES (FKs don’t auto-index in Postgres)
CREATE INDEX addresses_users_id_fk ON addresses (user_id);
CREATE INDEX fk_category ON products (category_id);
CREATE INDEX fk_wishlist_on_user ON wishlist (user_id);
