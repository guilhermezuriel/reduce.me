package com.guilhermezuriel.reduceme.application.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Key {

    @Id
    private UUID keyId;

    private String keyHash;

    private String originalUrl;

}
