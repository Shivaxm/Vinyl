DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'carts' AND column_name = 'user_id'
    ) THEN
        ALTER TABLE carts ADD COLUMN user_id BIGINT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_carts_user'
    ) THEN
        ALTER TABLE carts
            ADD CONSTRAINT fk_carts_user
                FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_carts_user_id ON carts(user_id);
