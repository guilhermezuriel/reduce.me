package com.guilhermezuriel.reduceme.application.config;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractSessionConfiguration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.nio.file.Paths;


@Configuration
@EnableCassandraRepositories(basePackages = "com.guilhermezuriel.reduceme.application.repository")
public class CassandraConfig  {

    @Value("${astra.bundle-path}")
    private String bundlePath;

    @Value("${astra.keyspace-name}")
    private String keyspace;

    @Bean
    public CqlSession cqlSession() {
       return CqlSession.builder()
                .withCloudSecureConnectBundle(Paths.get(bundlePath))
                .withKeyspace(keyspace)
                .build();
    }

    @Bean
    public CassandraTemplate cassandraTemplate(CqlSession cqlSession) {
        return new CassandraTemplate(cqlSession);
    }

}