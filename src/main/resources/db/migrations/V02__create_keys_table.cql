CREATE TABLE IF NOT EXISTS my_keyspace.keys (
       id UUID PRIMARY KEY,
       key_hash TEXT,
       original_url TEXT,
       created_at TIMESTAMP,
       expires_at timestamp,
       description varchar,
       counter int
);