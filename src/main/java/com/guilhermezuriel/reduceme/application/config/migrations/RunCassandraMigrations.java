package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
        this.createPublicIfNotExists();
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint)
                .withLocalDatacenter("datacenter1")
                .withKeyspace("public")
                .build()){
            var existsMigrationSystem =  RunCassandraMigrations.tableExists(session, "migration_system");
            if(!existsMigrationSystem){
                RunCassandraMigrations.createMigrationSystemTable(session);
            }
            checkFiles(session);
        }
        catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init(){
        var contactPoint = new InetSocketAddress(contactPoints, port);
        this.setContactPoint(contactPoint);
        log.info("Executing migrations");
    }

    private void createPublicIfNotExists(){
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint)
                .withLocalDatacenter("datacenter1")
                .build()){
            var createPublicIfNotExists = """
                CREATE KEYSPACE IF NOT EXISTS public
                WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};
                """;
            session.execute(createPublicIfNotExists);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void checkFiles(CqlSession session){
        String lastMigrationName = "";
        long lastMigrationRank = 0;
        Row lastMigrationExecuted = RunCassandraMigrations.lastMigrationExecuted(session);
        if(lastMigrationExecuted!= null){
            lastMigrationName = lastMigrationExecuted.getString("migration_name");
            lastMigrationRank = lastMigrationExecuted.getLong("installed_rank");

        }
        final String finalLastMigrationName = lastMigrationName;
        AtomicBoolean executed = new AtomicBoolean(true);
        AtomicLong executedCount = new AtomicLong(lastMigrationRank);
        if(Objects.isNull(finalLastMigrationName) || finalLastMigrationName.isBlank()){
            executed.set(false);
        }
        try (Stream<Path> paths = Files.list(Paths.get("src/main/resources/migrations")).sorted()) {
            paths.forEach(path -> {
                readFile(path, session, executed.get(), executedCount);
                if(Objects.equals(path.getFileName().toString(), finalLastMigrationName)){
                    executed.set(false);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Row lastMigrationExecuted(CqlSession session) {
        String query = """
                SELECT *
                FROM public.migration_system
                WHERE version_name = '1.0'
                ORDER BY installed_rank DESC
                LIMIT 1;
                """;
        ResultSet resultSet = session.execute(query);
        return resultSet.one();
    }

    private static void createMigrationSystemTable(CqlSession session) {
        var query = """
                    CREATE TABLE migration_system(
                         version_name varchar,
                         installed_rank bigint,
                         migration_name varchar,
                         checksum bigint,
                         primary key(version_name, installed_rank)) WITH CLUSTERING ORDER BY (installed_rank DESC);
                """;
        session.execute(query);
    }

    private void readFile(Path file, CqlSession session, boolean executed, AtomicLong lastMigrationRank) {
        List<String> lines;
        String migrationName = file.getFileName().toString();
        try {
            lines = Files.readAllLines(file);
            String content = String.join("\n", lines);
            long checksum = QueryUtils.calculateChecksum(content);
            if(executed){
                long checksumStored = RunCassandraMigrations.returnStoredChecksum(session, file.getFileName().toString());
                if(checksum != checksumStored){
                    throw new RuntimeException("Error while verifying migration: "+ migrationName + "\n Checksum error: Migration was modified \n"+ checksumStored + " != " + checksum);
                }
            }else {
                lastMigrationRank.set(lastMigrationRank.get() + 1);
                log.info("Executing migration ["+lastMigrationRank.get()+"]: "+migrationName);
                RunCassandraMigrations.registerMigrationOnSystem(session, lastMigrationRank.get(), file.getFileName().toString(), "1.0", checksum);
                session.execute(content);
            }
        }catch (IOException | RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void registerMigrationOnSystem(CqlSession session, Long installedRank, String migrationName, String versionName, Long checksum){
        String query = "INSERT INTO migration_system (installed_rank, migration_name, version_name, checksum) " +
                "VALUES  (?, ?, ?, ?)";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(installedRank, migrationName, versionName, checksum);

        session.execute(boundStatement);
    }

    private static long returnStoredChecksum(CqlSession session, String versioName) {
        String query = "SELECT checksum FROM migration_system " +
                "WHERE migration_name = ? ALLOW FILTERING;";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(versioName);

        ResultSet resultSet = session.execute(boundStatement);
        Row row = resultSet.one();

        if(row != null){
           return row.getLong("checksum");
        }

        throw new RuntimeException("Error while verifying migration: "+ versioName);
    }

    private static boolean columnExists(CqlSession session, String keyspace, String tableName, String columnName) {
        String query = "SELECT column_name FROM system_schema.columns " +
                "WHERE keyspace_name = ? AND table_name = ? AND column_name = ?";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(keyspace, tableName, columnName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.one() != null;
    }

    private static boolean tableExists(CqlSession session, String tableName) {
        String query = "SELECT table_name FROM system_schema.tables " +
                "WHERE keyspace_name = 'public' AND table_name = ? ;";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(tableName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.one() != null;
    }

}

