package com.guilhermezuriel.reduceme.application.config.infra.database.connection.local;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.guilhermezuriel.reduceme.application.config.infra.database.connection.local.queries.CqlMigrationsUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.cassandra.migrations.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class RunCassandraMigrations implements InitializingBean {

    private final CqlSession session;

    @Override
    public void afterPropertiesSet() throws IOException {
            MigrationService migrationService = new MigrationService(session);
            migrationService.createPublicSchemaIfNotExists();
            var existsMigrationSystem =  migrationService.tableExists( "migration_system");
            if(!existsMigrationSystem){
                migrationService.createMigrationSystemTable();
            }
            checkFiles(migrationService);
    }

    @PostConstruct
    public void init(){
        log.info("Executing migrations");
    }

    private void checkFiles(MigrationService migrationService) throws IOException {
        String lastMigrationName = "";
        long lastMigrationRank = 0;
        Row lastMigrationExecuted = migrationService.lastMigrationExecuted();
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
        String resourcesPath = "classpath:/db/migrations/*.cql";
        try {
        Resource[] resources = Arrays.stream(new PathMatchingResourcePatternResolver().getResources(resourcesPath))
                .filter(resource -> CqlMigrationsUtils.isValidMigrationPattern(resource.getFilename()))
                .toArray(Resource[]::new);
        Arrays.sort(resources, Comparator.comparing(resource -> {
                try{
                    return Objects.requireNonNull(resource.getFilename());}
                catch (Exception e){
                    return "";
                }}));
        for (Resource resource : resources) {
            readFile(resource, migrationService, executed.get(), executedCount);
            if (Objects.equals(resource.getFilename(), finalLastMigrationName)) {
                executed.set(false);
                log.info("Executing migration [{}]: {}", lastMigrationRank, lastMigrationName);
            }
        }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFile(Resource file, MigrationService migrationService, boolean executed, AtomicLong lastMigrationRank) {
        String content;
        String migrationName = file.getFilename();
        String version = CqlMigrationsUtils.extractVersion(migrationName);
        try {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            }
            long checksum = CqlMigrationsUtils.calculateChecksum(content);
            if(executed){
                long checksumStored = migrationService.returnStoredChecksum(version, migrationName);
                if(checksum != checksumStored){
                    throw new RuntimeException("Error while verifying migration: "+ migrationName + "\n Checksum error: Migration was modified \n"+ checksumStored + " != " + checksum);
                }
            }else {
                lastMigrationRank.set(lastMigrationRank.get() + 1);
                log.info("Executing migration [{}]: {}", lastMigrationRank.get(), migrationName);
                migrationService.registerMigrationOnSystem(lastMigrationRank.get(), migrationName, version, checksum);
                migrationService.executeQueryString(content);
            }
        }catch (IOException | RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

}

