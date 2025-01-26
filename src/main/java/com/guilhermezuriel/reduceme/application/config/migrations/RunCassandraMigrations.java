package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import com.guilhermezuriel.reduceme.application.config.CassandraConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.cassandra.core.cql.generator.CqlGenerator;
import org.springframework.data.cassandra.core.cql.keyspace.*;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunCassandraMigrations implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.v1_create_key_space();
        this.v2_create_key_table();
    }

    private final InetSocketAddress contactPoint = new InetSocketAddress("cassandra", 9042);

    public void v1_create_key_space() {
        log.info("Executing v1_create_my_keyspace");
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint)
                .withLocalDatacenter("datacenter1")
                .build()) {
            CqlSpecification createKeyspace = SpecificationBuilder.createKeyspace("my_keyspace")
                    .ifNotExists()
                    .with(KeyspaceOption.REPLICATION, KeyspaceAttributes.newSimpleReplication())
                    .with(KeyspaceOption.DURABLE_WRITES, true);
            String cql = CqlGenerator.toCql(createKeyspace);
            session.execute(cql);
        }catch (Exception e) {
            log.error("Some error occurred while executing v1_create_my_keyspace", e);
        }
    }

    public void v2_create_key_table() throws Exception {
        log.info("Executing v2_create_key_table");
        try(CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint)
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
            log.error("Some error occurred while executing v2_create_key_table", e);
        }
    }
}
