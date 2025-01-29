package com.guilhermezuriel.reduceme.application.services.keygen.dto;

import java.time.LocalDateTime;

public record KeyDto(
        String reducedUrl,
        LocalDateTime expirationDate
) {
    public static KeyDto of(String reducedUrl,  LocalDateTime expirationDate) {
        return new KeyDto(reducedUrl, expirationDate);
    }

    @Override
    public String toString() {
        return "{ reducedUrl: "+this.reducedUrl+", expiresAt: "+this.expirationDate+"}";
    }
}
