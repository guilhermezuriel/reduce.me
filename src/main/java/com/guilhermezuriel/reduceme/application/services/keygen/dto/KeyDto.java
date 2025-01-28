package com.guilhermezuriel.reduceme.application.services.keygen.dto;

import com.guilhermezuriel.reduceme.application.model.Key;

import java.time.LocalDateTime;

public record KeyDto(
        String keyHash,
        LocalDateTime createdDate
) {
    public static KeyDto of(Key key) {
        return new KeyDto(key.getKeyHash(), key.getCreatedAt());
    }

    @Override
    public String toString() {
        return "{ hash: "+this.keyHash+", createdAt: "+this.createdDate+"}";
    }
}
