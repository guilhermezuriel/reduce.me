package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.guilhermezuriel.reduceme.application.config.migrations.queries.MigrationSchemaCql;
import org.springframework.stereotype.Service;

@Service
public class MigrationService {

    private final CqlSession session;

    public MigrationService(CqlSession session) {
        this.session = session;
    }

    public void executeQueryString(String query) {
        session.execute(query);
    }

    public boolean tableExists(String tableName) {
        PreparedStatement prepared = session.prepare(MigrationSchemaCql.selectTableExists());
        BoundStatement bound = prepared.bind(tableName);
        return session.execute(bound).one() != null;
    }

    public void createPublicSchemaIfNotExists() {
        session.execute(MigrationSchemaCql.createKeyspaceIfNotExists());
    }

    public void createMigrationSystemTable() {
        session.execute(MigrationSchemaCql.createMigrationSystemTable());
    }

    public Row lastMigrationExecuted() {
        return session.execute(MigrationSchemaCql.selectLastExecutedMigration()).one();
    }

    public void registerMigrationOnSystem(Long installedRank, String migrationName, String version, Long checksum) {
        PreparedStatement prepared = session.prepare(MigrationSchemaCql.insertMigrationRecord());
        BoundStatement bound = prepared.bind(installedRank, migrationName, version, checksum);
        session.execute(bound);
    }

    public long returnStoredChecksum(String version, String migrationName) {
        PreparedStatement prepared = session.prepare(MigrationSchemaCql.selectChecksumByVersion());
        BoundStatement bound = prepared.bind(version);
        Row row = session.execute(bound).one();
        if (row != null) {
            return row.getLong("checksum");
        }
        throw new RuntimeException("Error while verifying migration: " + migrationName);
    }
}
