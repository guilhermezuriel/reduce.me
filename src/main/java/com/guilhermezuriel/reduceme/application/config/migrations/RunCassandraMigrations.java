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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunCassandraMigrations implements InitializingBean {
    private final Session session;
    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    private final ResourceLoader resourceLoader;

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
        catch (RuntimeException | IOException e) {
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

    private void checkFiles(CqlSession session) throws IOException {
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
//        try (Stream<Path> paths = Files.list(Paths.get("src/main/resources/db/migrations")).sorted()) {
//            paths.forEach(path -> {
//                readFile(path, session, executed.get(), executedCount);
//                if(Objects.equals(path.getFileName().toString(), finalLastMigrationName)){
//                    executed.set(false);
//                }
//            });
        try {
        List<Resource> resources = Arrays.stream(new PathMatchingResourcePatternResolver().getResources("classpath:/db/migrations/*.cql")).sorted().toList();
            resources.forEach(path -> {
                readFile(path, session, executed.get(), executedCount);
                if (Objects.equals(path.getFilename(), finalLastMigrationName)) {
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

    private void readFile(Resource file, CqlSession session, boolean executed, AtomicLong lastMigrationRank) {
        String content;
        String migrationName = file.getFilename();
        try {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            }
            long checksum = QueryUtils.calculateChecksum(content);
            if(executed){
                long checksumStored = RunCassandraMigrations.returnStoredChecksum(session, migrationName);
                if(checksum != checksumStored){
                    throw new RuntimeException("Error while verifying migration: "+ migrationName + "\n Checksum error: Migration was modified \n"+ checksumStored + " != " + checksum);
                }
            }else {
                lastMigrationRank.set(lastMigrationRank.get() + 1);
                log.info("Executing migration [{}]: {}", lastMigrationRank.get(), migrationName);
                RunCassandraMigrations.registerMigrationOnSystem(session, lastMigrationRank.get(), migrationName, "1.0", checksum);
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

    private static boolean tableExists(CqlSession session, String tableName) {
        String query = "SELECT table_name FROM system_schema.tables " +
                "WHERE keyspace_name = 'public' AND table_name = ? ;";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(tableName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.one() != null;
    }

}

