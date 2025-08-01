package com.guilhermezuriel.reduceme.application.config.infra.database.connection.local.queries;

public final class MigrationSchemaCql {

    private static final String KEYSPACE = "public";
    private static final String TABLE = "migration_system";

    private MigrationSchemaCql() {}

    public static String createKeyspaceIfNotExists() {
        return String.format("""
            CREATE KEYSPACE IF NOT EXISTS %s 
            WITH replication = {
                'class': 'SimpleStrategy', 
                'replication_factor': 3
            };
            """, KEYSPACE);
    }

    public static String createMigrationSystemTable() {
        return String.format("""
            CREATE TABLE IF NOT EXISTS %s.%s (
                partition_text TEXT,
                version VARCHAR,
                installed_rank BIGINT,
                migration_name TEXT,
                checksum BIGINT,
                PRIMARY KEY (partition_text, installed_rank)
            ) WITH CLUSTERING ORDER BY (installed_rank DESC);
            """, KEYSPACE, TABLE);
    }

    public static String selectTableExists() {
        return String.format("""
            SELECT table_name FROM system_schema.tables 
            WHERE keyspace_name = '%s' AND table_name = ?;
            """, KEYSPACE);
    }

    public static String selectLastExecutedMigration() {
        return String.format("""
            SELECT * FROM %s.%s
            WHERE partition_text = 'last_migration'
            LIMIT 1;
            """, KEYSPACE, TABLE);
    }

    public static String insertMigrationRecord() {
        return String.format("""
            INSERT INTO %s.%s (partition_text, installed_rank, migration_name, version, checksum)
            VALUES ('last_migration', ?, ?, ?, ?);
            """, KEYSPACE, TABLE);
    }

    public static String selectChecksumByMigrationName() {
        return String.format("""
            SELECT checksum FROM %s.%s
            WHERE migration_name = ? ALLOW FILTERING;
            """, KEYSPACE, TABLE);
    }

    public static String selectChecksumByVersion() {
        return String.format("""
            SELECT checksum FROM %s.%s
            WHERE version = ? ALLOW FILTERING;
            """, KEYSPACE, TABLE);
    }
}
