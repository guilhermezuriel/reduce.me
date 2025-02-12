package com.guilhermezuriel.reduceme.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;

@Table
@Getter
@Setter
@AllArgsConstructor
@Builder
public class CqlMigrationSystem {

    @PrimaryKey@Column("installed_rank")
    private final Long installedRank;
    @Column("key_hash")
    private final String keyHash;
    @Column("original_url")
    private final String originalUrl;
    @Column("created_at")@Builder.Default
    private final LocalDateTime createdAt = LocalDateTime.now();
    @Column("expires_at")
    private final LocalDateTime expiresAt;

}

