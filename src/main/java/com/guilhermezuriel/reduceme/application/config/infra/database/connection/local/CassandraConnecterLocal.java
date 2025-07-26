package com.guilhermezuriel.reduceme.application.config.infra.database.connection.local;

import com.datastax.oss.driver.api.core.CqlSession;
import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import com.guilhermezuriel.reduceme.application.config.infra.database.connection.CassandraConnecterPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@Profile("local")
public class CassandraConnecterLocal implements CassandraConnecterPort {

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Value("${spring.cassandra.username}")
    private String username;

    @Value("${spring.cassandra.password}")
    private String password;

    @Override
    public CqlSession connect() {
            var contactPoint = new InetSocketAddress(contactPoints, port);
            return  CqlSession.builder()
                    .addContactPoint(contactPoint)
                    .withAuthCredentials(username, password)
                    .withLocalDatacenter("datacenter1")
                    .build();
    }

}