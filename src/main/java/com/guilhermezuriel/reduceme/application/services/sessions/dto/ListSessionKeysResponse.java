package com.guilhermezuriel.reduceme.application.services.sessions.dto;

import com.guilhermezuriel.reduceme.application.services.keygen.dto.CompleteKeyResponse;

import java.util.List;

public record ListSessionKeysResponse(
        String sessionId,
        List<CompleteKeyResponse> keys
) {
    public static ListSessionKeysResponse from(String sessionId, List<CompleteKeyResponse> keys) {
        return new ListSessionKeysResponse(sessionId, keys);
    }
}
