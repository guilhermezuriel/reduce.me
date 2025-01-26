package com.guilhermezuriel.reduceme.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration{

    @Override
    protected String getContactPoints() {
        return "cassandra";
    }

    @Override
    protected String getKeyspaceName() {
        return "system";
    }
}
