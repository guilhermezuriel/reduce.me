package com.guilhermezuriel.reduceme.application.config.infra.database.connection.prd;

import com.datastax.oss.driver.api.core.CqlSession;
import com.guilhermezuriel.reduceme.application.config.infra.database.connection.CassandraConnecterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Profile("prd")
@Component
public class CassandraConnecterPrd implements CassandraConnecterPort {


    @Value("${astra.bundle-path}")
    private String bundlePath;

    @Value("${astra.keyspace-name}")
    private String keyspace;

    @Value("${astra.auth.credentials.username}")
    private String username;

    @Value("${astra.auth.credentials.password}")
    private String password;


    private static final Logger log = LoggerFactory.getLogger(CassandraConnecterPrd.class);


    @Override
    public CqlSession connect() {

        Path bundle = Paths.get(bundlePath);
        if (!Files.exists(bundle)) {
            throw new IllegalStateException("Secure connect bundle NOT found at:" + bundle.toAbsolutePath());
        }
        try {
            var builder = CqlSession.builder()
                    .withCloudSecureConnectBundle(bundle)
                    .withAuthCredentials(username, password)
                    .withKeyspace(keyspace);
            return builder.build();
        } catch (Exception e) {
            log.error("Failed to create CqlSession with bundle {}: {}", bundle.toAbsolutePath(), e.getMessage(), e);
            throw e;
        }
    }

}
