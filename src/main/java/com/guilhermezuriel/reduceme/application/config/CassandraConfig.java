package com.guilhermezuriel.reduceme.application.config;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration{

    @Override
    protected String getContactPoints() {
        return "127.0.0.1";
    }

    @Override
    protected String getKeyspaceName() {
        return "my_keyspace";
    }
}
