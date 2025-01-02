package com.guilhermezuriel.reduceme.application.repository;

import com.guilhermezuriel.reduceme.application.model.Key;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface KeyRepository extends CassandraRepository<Key, UUID> {

    boolean existsKeyByKey_hash(String key);

    Optional<Key> findKeyByKey_hash(String key);

}
