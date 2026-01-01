ALTER TABLE carts ADD COLUMN user_id BIGINT;

ALTER TABLE carts
    ADD CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

CREATE INDEX idx_carts_user_id ON carts(user_id);
