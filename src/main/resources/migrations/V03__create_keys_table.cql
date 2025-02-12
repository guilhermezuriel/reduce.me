CREATE TABLE IF NOT EXISTS keys (
       id UUID PRIMARY KEY,
       key_hash TEXT,
       original_url TEXT,
       created_at TIMESTAMP,
);