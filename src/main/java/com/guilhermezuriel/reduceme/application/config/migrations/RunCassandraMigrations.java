package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunCassandraMigrations implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.v1_create_key_table();
    }

    @Value("${spring.cassandra.contact-points}")
    private String contactPoints;

    @Value("${spring.cassandra.port}")
    private int port;

    @Getter
    @Setter
    private InetSocketAddress contactPoint;

    @PostConstruct
    public void init() {
        log.info("------------------- Verifying migrations -----------------");
        var contactPoint = new InetSocketAddress(contactPoints, port);
        this.setContactPoint(contactPoint);
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
            log.error("Some error occurred while executing v1_create_key_table", e);
        }
    }
}
