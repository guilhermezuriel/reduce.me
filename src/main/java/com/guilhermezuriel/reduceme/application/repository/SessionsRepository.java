package com.guilhermezuriel.reduceme.application.repository;

import com.datastax.oss.driver.api.core.session.Session;
import com.guilhermezuriel.reduceme.application.model.Sessions;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionsRepository extends CassandraRepository<Sessions, String> {

    Sessions findSessionsBySessionId(String sessionId);
}
