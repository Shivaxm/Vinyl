ALTER TABLE carts ADD COLUMN guest_token VARCHAR(255);

CREATE INDEX idx_carts_guest_token ON carts(guest_token);