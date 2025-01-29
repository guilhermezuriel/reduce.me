package com.guilhermezuriel.reduceme.application.config.migrations;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.session.Session;
import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.data.cassandra.core.cql.generator.CqlGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunCassandraMigrations implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.v1_create_key_table();
        this.v2_update_key_table();
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

    private static boolean columnExists(CqlSession session, String keyspace, String tableName, String columnName) {
        String query = "SELECT column_name FROM system_schema.columns " +
                "WHERE keyspace_name = ? AND table_name = ? AND column_name = ?";
        PreparedStatement preparedStatement = session.prepare(query);
        BoundStatement boundStatement = preparedStatement.bind(keyspace, tableName, columnName);

        ResultSet resultSet = session.execute(boundStatement);

        return resultSet.one() != null;
    }
}
