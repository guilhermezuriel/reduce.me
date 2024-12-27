package com.guilhermezuriel.reduceme.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table
@Data
@Builder
@AllArgsConstructor
public class Key {

    @PrimaryKey @Id
    private final UUID key_id;

    private String key_hash;

    private String original_url;

}
