package com.guilhermezuriel.reduceme.application.config.migrations.queries;

public class QueryService {

    public static String createPublicSchemaIfNotExists() {
        return "CREATE KEYSPACE IF NOT EXISTS public WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};";
    }

    public static String createMigratioSystemTable() {
        return  """
                    CREATE TABLE public.migration_system(
                         version_name varchar,
                         installed_rank bigint,
                         migration_name varchar,
                         checksum bigint,
                         primary key(version_name, installed_rank)) WITH CLUSTERING ORDER BY (installed_rank DESC);
                """;
    }

}
