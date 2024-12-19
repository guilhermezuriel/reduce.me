package com.guilhermezuriel.reduceme.application.config;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

public class CassandraConfig extends AbstractCassandraConfiguration implements BeanClassLoaderAware {

    @Override
    protected String getContactPoints() {
        return "127.0.0.1";
    }

    @Override
    protected String getKeyspaceName() {
        return "keyspace";
    }
}
