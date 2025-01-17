package com.guilhermezuriel.reduceme.application.config;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.cql.keyspace.*;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CreateKeyspaceConfiguration extends AbstractCassandraConfiguration implements BeanClassLoaderAware {

    @Override
    protected String getKeyspaceName() {
        return "";
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {

        CreateKeyspaceSpecification specification = SpecificationBuilder.createKeyspace("my_keyspace")
                .with(KeyspaceOption.DURABLE_WRITES, true)
                .withNetworkReplication(DataCenterReplication.of("foo", 1), DataCenterReplication.of("bar", 2));

        return Arrays.asList(specification);
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return Arrays.asList(DropKeyspaceSpecification.dropKeyspace("my_keyspace"));
    }

    // ...
}