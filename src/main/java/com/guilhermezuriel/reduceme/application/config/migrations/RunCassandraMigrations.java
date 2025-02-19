package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import com.datastax.oss.driver.api.core.session.Session;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunCassandraMigrations implements InitializingBean {
    private final Session session;
    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Getter
    @Setter
    private InetSocketAddress contactPoint;

    @Override
    public void afterPropertiesSet(){
        var contactPoint = this.getContactPoint();
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint)
                .withLocalDatacenter("datacenter1")
                .build()){
            //todo : verify if the cql_migration_system table is present in the bank then execute migrations
            var existsMigrationSystem =  RunCassandraMigrations.tableExists(session,"system", "migration_system");
            if(!existsMigrationSystem){
                RunCassandraMigrations.createMigrationSystemTable(session);
            }
            this.checkFiles(session);
        }
        catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init(){
        log.info("------------------- Verifying migrations -----------------");
        var contactPoint = new InetSocketAddress(contactPoints, port);
        this.setContactPoint(contactPoint);
    }

    private void checkFiles(CqlSession session){
        String lastMigrationName;
        Integer lastMigrationRank;
        ResultSet lastMigrationExecuted = RunCassandraMigrations.lastMigrationExecuted(session);
        for(Row row : lastMigrationExecuted){
            lastMigrationName = row.getString("version_name");
            lastMigrationRank = row.getInt("installed_rank");
        }
        //TODO: Verify if the path file is the last migration then execute the rest
        // TODO: Increment the insert of the rank upon the last installed rank value
        try (Stream<Path> paths = Files.list(Paths.get("src/main/resources/migrations"))) {
            paths.forEach(path -> readFile(path, session));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResultSet lastMigrationExecuted(CqlSession session) {
        String query = """
                SELECT installed_rank, version_name 
                FROM migration_system
                ORDER BY installed_rank DESC
                LIMIT 1;
                """;
        ResultSet resultSet = session.execute(query);
        return resultSet;
    }

    private static void createMigrationSystemTable(CqlSession session) {
        var query = """
                    CREATE TABLE migration_system(
                        installed_rank int,
                        version_name varchar,
                        checksum bigint,
                        primary key (version_name, installed_rank)
                        ) WITH CLUSTERING ORDER BY (installed_rank asc);
                """;
        session.execute(query);
    }

    private void readFile(Path file, CqlSession session) {
        List<String> lines;
        boolean executed = RunCassandraMigrations.itWasExecuted(file.getFileName().toString());
        try {
            lines = Files.readAllLines(file);
            String content = String.join("\n", lines);
            int checksum = QueryUtils.calculateChecksum(content);
            if(executed){
                Integer checksumStored = 0;
                ResultSet getChecksumStored = RunCassandraMigrations.migrationExecuted(session, file.getFileName().toString());
                for(Row row : getChecksumStored){
                    checksumStored = row.getInt("checksum");
                    break;
                }
                if(checksum != checksumStored){
                    throw new RuntimeException("Checksum error: " + checksumStored + " != " + checksum);
                }
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static boolean itWasExecuted(String fileName){
        return fileName != null && !fileName.isEmpty();
    }

//    public void v1_create_key_table() throws Exception {
//        log.info("Executing v1_create_key_table");
//        var contactPoint1 = this.getContactPoint();
//        try(CqlSession session = CqlSession.builder()
//                .addContactPoint(contactPoint1)
//                .withKeyspace("my_keyspace")
//                .withLocalDatacenter("datacenter1")
//                .build()) {
//            String cql = """
//                    CREATE TABLE IF NOT EXISTS keys (
//                        id UUID PRIMARY KEY,
//                        key_hash TEXT,
//                        original_url TEXT,
//                        created_at TIMESTAMP,
//                    );
//                    """;
//            session.execute(cql);
//        }catch (Exception e) {
//            throw ApplicationException.builder()
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .message("Some error occurred while executing v1_create_key_table: " + e.getMessage()).build();
//        }
//    }
//
//    public void v2_update_key_table() throws Exception {
//        log.info("Executing v2_update_key_table");
//        var contactPoint1 = this.getContactPoint();
//        String keyspace  = "my_keyspace";
//
//        try(CqlSession session = CqlSession.builder()
//                .addContactPoint(contactPoint1)
//                .withKeyspace(keyspace)
//                .withLocalDatacenter("datacenter1")
//                .build()) {
//            var columnExists = RunCassandraMigrations.columnExists(session, keyspace, "keys", "expires_at");
//            if (columnExists) {
//                return;
//            }
//            String addColumn = QueryUtils.addColumn("keys", "expires_at", "timestamp");
//            session.execute(addColumn);
//        }catch (Exception e) {
//            throw ApplicationException.builder()
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .message("Some error occurred while executing v2_create_key_table: " + e.getMessage()).build();
//        }
//    }

    private static ResultSet migrationExecuted(CqlSession session, String versioName) {
        String query = "SELECT checksum FROM migration_system " +
                "WHERE version_name = ?";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(versioName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet;
    }

    private static boolean columnExists(CqlSession session, String keyspace, String tableName, String columnName) {
        String query = "SELECT column_name FROM system_schema.columns " +
                "WHERE keyspace_name = ? AND table_name = ? AND column_name = ?";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(keyspace, tableName, columnName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.one() != null;
    }

    private static boolean tableExists(CqlSession session, String keyspace, String tableName) {
        String query = "SELECT column_name FROM system_schema.tables " +
                "WHERE keyspace_name = ? AND table_name = ? ;";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(keyspace, tableName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.one() != null;}
    }
}
