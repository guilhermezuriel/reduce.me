package com.guilhermezuriel.reduceme.application.config.infra.database.connection;

import com.datastax.oss.driver.api.core.CqlSession;

public interface CassandraConnecterPort {

    CqlSession connect();
}
