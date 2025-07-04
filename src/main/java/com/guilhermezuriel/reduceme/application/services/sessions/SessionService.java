package com.guilhermezuriel.reduceme.application.services.sessions;

import com.guilhermezuriel.reduceme.application.model.Sessions;
import com.guilhermezuriel.reduceme.application.repository.SessionsRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionsRepository sessionsRepository;

    private final int ONE_YEAR_IN_SECONDS = 60 * 60 * 24 * 365;

    public Cookie initializeSession() {
        String sessionId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("session_id", sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(ONE_YEAR_IN_SECONDS);
        Sessions sessions = new Sessions(sessionId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now().plusSeconds(ONE_YEAR_IN_SECONDS), null);
        sessionsRepository.insert(sessions);
        return cookie;
    }

    public void updateSession(String sessionId){
        Sessions sessions = sessionsRepository.findById(sessionId).orElseThrow();
        sessions.setUpdatedAt(LocalDateTime.now());
        sessionsRepository.insert(sessions);
    }

    public static void addNewKeyToList(SessionsRepository sessionsRepository, String sessionId, UUID keyId) {
        Sessions session = sessionsRepository.findSessionsBySessionId(sessionId);
        List<UUID> newList = session.getKeysId() == null ? new ArrayList<>() : session.getKeysId();
        newList.add(keyId);
        session.setKeysId(newList);
        sessionsRepository.insert(session);
    }

    public static List<UUID> returnAllKeysFromSession(SessionsRepository sessionsRepository, String sessionId) {
        Sessions session = sessionsRepository.findSessionsBySessionId(sessionId);
        return session.getKeysId() == null ? new ArrayList<>() : session.getKeysId();
    }
}
