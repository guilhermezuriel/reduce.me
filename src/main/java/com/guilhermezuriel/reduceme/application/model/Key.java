package com.guilhermezuriel.reduceme.application.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(value = "keys", keyspace = "my_keyspace")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Key {

    @PrimaryKey
    private final UUID id;
    @Column("key_hash")
    private final String keyHash;
    @Column("original_url")
    private final String originalUrl;
    @Column("created_at")@Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public String toString() {
        return "Key{" + ", keyHash='" + keyHash +
                ", originalUrl='" + originalUrl +
                ", createdAt=" + createdAt + '}';
    }
}
