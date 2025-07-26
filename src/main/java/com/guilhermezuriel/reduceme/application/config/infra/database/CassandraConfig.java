package com.guilhermezuriel.reduceme.application.config.infra.database;


import com.datastax.oss.driver.api.core.CqlSession;
import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import com.guilhermezuriel.reduceme.application.config.infra.database.connection.CassandraConnecterPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.http.HttpStatus;


@Configuration
@EnableCassandraRepositories(basePackages = "com.guilhermezuriel.reduceme.application.repository")
public class CassandraConfig  {

    private final CassandraConnecterPort cassandraConnecterPort;

    public CassandraConfig(CassandraConnecterPort cassandraConnecterPort) {
        this.cassandraConnecterPort = cassandraConnecterPort;
    }

    @Bean
    public CqlSession cqlSession(){
        try {
            return cassandraConnecterPort.connect();
        } catch (Exception e) {
        throw ApplicationException.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("CONNECTION ERROR: " + e.getMessage())
                .build();
        }
    }

    @Bean
    public CassandraTemplate cassandraTemplate(CqlSession cqlSession) {
        return new CassandraTemplate(cqlSession);
    }

}