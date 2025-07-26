package com.guilhermezuriel.reduceme.application.config.infra.database.connection.local;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.guilhermezuriel.reduceme.application.config.infra.database.connection.local.queries.MigrationSchemaCql;
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
        PreparedStatement prepared = this.session.prepare(MigrationSchemaCql.selectTableExists());
        BoundStatement bound = prepared.bind(tableName);
        return this.session.execute(bound).one() != null;
    }

    public void createPublicSchemaIfNotExists() {
        this.session.execute(MigrationSchemaCql.createKeyspaceIfNotExists());
    }

    public void createMigrationSystemTable() {
        session.execute(MigrationSchemaCql.createMigrationSystemTable());
    }

    public Row lastMigrationExecuted() {
        return this.session.execute(MigrationSchemaCql.selectLastExecutedMigration()).one();
    }

    public void registerMigrationOnSystem(Long installedRank, String migrationName, String version, Long checksum) {
        PreparedStatement prepared = this.session.prepare(MigrationSchemaCql.insertMigrationRecord());
        BoundStatement bound = prepared.bind(installedRank, migrationName, version, checksum);
        this.session.execute(bound);
    }

    public long returnStoredChecksum(String version, String migrationName) {
        PreparedStatement prepared = this.session.prepare(MigrationSchemaCql.selectChecksumByVersion());
        BoundStatement bound = prepared.bind(version);
        Row row = this.session.execute(bound).one();
        if (row != null) {
            return row.getLong("checksum");
        }
        throw new RuntimeException("Error while verifying migration: " + migrationName);
    }
}
