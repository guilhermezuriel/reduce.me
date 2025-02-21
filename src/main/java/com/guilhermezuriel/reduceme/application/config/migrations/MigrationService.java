package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.guilhermezuriel.reduceme.application.config.migrations.queries.QueryService;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

    private final CqlSession session;

    public MigrationService(CqlSession session) {
        this.session = session;

    }

    protected void executeQueryString(String query) {
        this.session.execute(query);
    }

    protected boolean tableExists(String tableName) {
        String query = "SELECT table_name FROM system_schema.tables " +
                "WHERE keyspace_name = 'public' AND table_name = ? ;";
        PreparedStatement preparedStatement = this.session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(tableName);

        ResultSet resultSet = this.session.execute(boundStatement);

        return resultSet.one() != null;
    }

    protected void createPublicSchemaIfNotExists() {
        String query = QueryService.createPublicSchemaIfNotExists();
        this.session.execute(query);
    }


    protected void createMigrationSystemTable() {
        String query = QueryService.createMigratioSystemTable();
        this.session.execute(query);
    }

    protected Row lastMigrationExecuted() {
        String query = """
                SELECT *
                FROM public.migration_system
                WHERE version_name = '1.0'
                ORDER BY installed_rank DESC
                LIMIT 1;
                """;
        ResultSet resultSet = this.session.execute(query);
        return resultSet.one();
    }

    protected void registerMigrationOnSystem(Long installedRank, String migrationName, String versionName, Long checksum){
        String query = "INSERT INTO migration_system (installed_rank, migration_name, version_name, checksum) " +
                "VALUES  (?, ?, ?, ?)";
        PreparedStatement preparedStatement = this.session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(installedRank, migrationName, versionName, checksum);
        this.session.execute(boundStatement);
    }

    protected long returnStoredChecksum(String versioName) {
        String query = "SELECT checksum FROM public.migration_system " +
                "WHERE migration_name = ? ALLOW FILTERING;";
        PreparedStatement preparedStatement = this.session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(versioName);

        ResultSet resultSet = this.session.execute(boundStatement);
        Row row = resultSet.one();

        if(row != null){
            return row.getLong("checksum");
        }

        throw new RuntimeException("Error while verifying migration: "+ versioName);
    }
}
