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


