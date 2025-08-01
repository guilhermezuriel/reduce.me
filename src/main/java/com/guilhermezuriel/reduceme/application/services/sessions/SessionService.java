package com.guilhermezuriel.reduceme.application.services.sessions;

import com.guilhermezuriel.reduceme.application.model.Sessions;
import com.guilhermezuriel.reduceme.application.repository.SessionsRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionsRepository sessionsRepository;

    private final int ONE_YEAR_IN_SECONDS = 60 * 60 * 24 * 365;

    private Sessions initializeSession(HttpServletResponse response) {
        String sessionId = UUID.randomUUID().toString();
        Sessions sessions = new Sessions(sessionId, new ArrayList<>(), LocalDateTime.now(), LocalDateTime.now().plusSeconds(ONE_YEAR_IN_SECONDS), null);
        sessionsRepository.insert(sessions);
        addCookie(sessionId, response);
        return sessions;
    }

    private void addCookie(String sessionId, HttpServletResponse response){
        Cookie cookie = new Cookie("session_id", sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(ONE_YEAR_IN_SECONDS);
        response.addCookie(cookie);
    }

    private Sessions updateSession(String sessionId, HttpServletResponse response) {
        Optional<Sessions> optionalSessions = sessionsRepository.findById(sessionId);
        if (optionalSessions.isEmpty()) {
            return this.initializeSession(response);
        }

        Sessions sessions = optionalSessions.get();
        sessions.setUpdatedAt(LocalDateTime.now());
        sessionsRepository.save(sessions);
        return sessions;
    }

    public Sessions manageSession(String sessionId, HttpServletResponse response){
        if (Objects.isNull(sessionId)) {
            return this.initializeSession(response);
        }
        return this.updateSession(sessionId, response);
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
