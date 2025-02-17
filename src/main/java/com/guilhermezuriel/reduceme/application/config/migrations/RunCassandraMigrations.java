package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
            var existsMigrationSystem =  RunCassandraMigrations.tableExists(session,"system", "cms_cql_migration_system");
            if(!existsMigrationSystem){

            }
            this.checkFiles();
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

    private void checkFiles(){
        try (Stream<Path> paths = Files.list(Paths.get("src/main/resources/migrations"))) {
            paths.forEach(this::readFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFile(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
            String content = String.join("\n", lines);
//            try(CqlSession session = CqlSession.builder().build()) {
//                RunCassandraMigrations.columnExists(session, "system", "cms_cql_migration_system", )
//            }
            int checksum = QueryUtils.calculateChecksum(content);
            int checksumStored = this.retrieveChecksumByVersionName(file.getFileName().toString());
            //TODO: Verify if it was executed
            //TODO: If it was executed -> Compare the checksum
            if(checksum != checksumStored){
                throw new RuntimeException("Checksum error: " + checksumStored + " != " + checksum);
            }
            //TODO: If was not executed -> Execute query + Store the executed query in the table

        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private int retrieveChecksumByVersionName(String versionName){
        return 0;
    }

    public void v1_create_key_table() throws Exception {
        log.info("Executing v1_create_key_table");
        var contactPoint1 = this.getContactPoint();
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint1)
                .withKeyspace("my_keyspace")
                .withLocalDatacenter("datacenter1")
                .build()) {
            String cql = """
                    CREATE TABLE IF NOT EXISTS keys (
                        id UUID PRIMARY KEY,
                        key_hash TEXT,
                        original_url TEXT,
                        created_at TIMESTAMP,
                    );
                    """;
            session.execute(cql);
        }catch (Exception e) {
            throw ApplicationException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Some error occurred while executing v1_create_key_table: " + e.getMessage()).build();
        }
    }

    public void v2_update_key_table() throws Exception {
        log.info("Executing v2_update_key_table");
        var contactPoint1 = this.getContactPoint();
        String keyspace  = "my_keyspace";

        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint1)
                .withKeyspace(keyspace)
                .withLocalDatacenter("datacenter1")
                .build()) {
            var columnExists = RunCassandraMigrations.columnExists(session, keyspace, "keys", "expires_at");
            if (columnExists) {
                return;
            }
            String addColumn = QueryUtils.addColumn("keys", "expires_at", "timestamp");
            session.execute(addColumn);
        }catch (Exception e) {
            throw ApplicationException.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Some error occurred while executing v2_create_key_table: " + e.getMessage()).build();
        }
    }

    private static ColumnDefinitions migrationExecuted(CqlSession session, String versioName) {
        String query = "SELECT checksum FROM cms_cql_migration_system " +
                "WHERE version_name = ?";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(versioName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.getColumnDefinitions();
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
