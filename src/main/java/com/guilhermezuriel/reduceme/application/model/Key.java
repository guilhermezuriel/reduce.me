package com.guilhermezuriel.reduceme.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table
@Data
@Builder
@AllArgsConstructor
public class Key {

    @PrimaryKey
    private final UUID id;

    private String key;

    private String originalURL;

}
