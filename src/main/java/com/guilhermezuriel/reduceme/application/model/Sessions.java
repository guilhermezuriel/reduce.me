package com.guilhermezuriel.reduceme.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table(value = "sessions", keyspace = "my_keyspace")
@Getter
@Setter
@AllArgsConstructor
public class Sessions {

    @PrimaryKey
    @Column(value = "session_id")
    private final String sessionId;

    @Column(value = "keys_id")
    private List<UUID> keysId;

    @Column(value = "session_start")
    private final LocalDateTime sessionStart;

    @Column(value = "session_end")
    private LocalDateTime sessionEnd;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;

}
