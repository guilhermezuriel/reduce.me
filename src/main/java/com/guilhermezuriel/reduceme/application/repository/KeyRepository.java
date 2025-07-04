package com.guilhermezuriel.reduceme.application.repository;

import com.guilhermezuriel.reduceme.application.model.Key;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface KeyRepository extends CassandraRepository<Key, UUID> {

    @Query(allowFiltering = true)
    Optional<Key> findKeyByKeyHash(String keyHash);

    @Query(allowFiltering = true, value = """
            select * from my_keyspace.keys where expires_at < toTimestamp(now()) ALLOW FILTERING;
            """)
    List<Key> findAllWithMoreThanExpirationDate();

    List<Key> findAllByIdIn(List<UUID> ids);
}
