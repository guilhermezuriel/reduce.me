CREATE TABLE IF NOT EXISTS my_keyspace.sessions (
                                                session_id VARCHAR PRIMARY KEY,
                                                keys_id LIST<UUID>,
                                                session_start TIMESTAMP,
                                                session_end TIMESTAMP,
                                                updated_at TIMESTAMP
);