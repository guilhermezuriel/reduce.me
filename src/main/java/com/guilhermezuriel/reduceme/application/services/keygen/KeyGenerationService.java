package com.guilhermezuriel.reduceme.application.services.keygen;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.model.Sessions;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import com.guilhermezuriel.reduceme.application.repository.SessionsRepository;
import com.guilhermezuriel.reduceme.application.services.keygen.dto.KeyDto;
import com.guilhermezuriel.reduceme.application.services.keygen.dto.CompleteKeyResponse;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import com.guilhermezuriel.reduceme.application.services.sessions.SessionService;
import com.guilhermezuriel.reduceme.application.services.sessions.dto.ListSessionKeysResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeyGenerationService {

    private final KeyRepository keyRepository;
    private final SessionsRepository sessionsRepository;

    @Value("${reduceme.api_url_base}")
    private String API_BASE_URL;

    public KeyDto generateKeys(CreateReducedUrlForm form) {
        Key keyEntity = this.createReducedUrlHash(form);
        return this.handleReducedUrlInformations(keyEntity);
    }

    public KeyDto generateKeys(CreateReducedUrlForm form, String sessionId) {
        Key keyEntity = this.createReducedUrlHash(form);
        assert sessionId != null;
        SessionService.addNewKeyToList(sessionsRepository, sessionId, keyEntity.getId());
        return this.handleReducedUrlInformations(keyEntity);
    }

    private Key createReducedUrlHash(CreateReducedUrlForm form){
        var isValidUrl = Utils.isValidUrl(form.url());
        if(Boolean.FALSE.equals(isValidUrl)){
            throw new RuntimeException("Invalid URL");
        }
        String keyHash = Utils.digestUrl(form.url());
        Key keyEntity =  Key.builder()
                .id(UUID.randomUUID())
                .originalUrl(form.url())
                .description(form.description())
                .keyHash(keyHash)
                .expiresAt(LocalDateTime.now().plusSeconds(60 * 60 * 24))
                .counter(0)
                .build();
        this.keyRepository.insert(keyEntity);
        return keyEntity;
    }

    public String getKeyByKeyHash(String keyHash) {
        Optional<Key> optionalKey = this.keyRepository.findKeyByKeyHash(keyHash);
        if(optionalKey.isEmpty()){
            throw ApplicationException.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Key Not Found")
                    .build();
        }
        Key key = optionalKey.get();
        if(key.getExpiresAt().isBefore(LocalDateTime.now())){
            throw ApplicationException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Key expired").build();
        }
        key.setCounter(key.getCounter() + 1);
        this.keyRepository.insert(key);
        return key.getOriginalUrl();
    }

    public void deleteKeyByKeyHash(String sessionId, String keyHash) {
        Sessions sessions = this.sessionsRepository.findSessionsBySessionId(sessionId);
        List<UUID> list = sessions.getKeysId();
        Key key = this.keyRepository.findKeyByKeyHash(keyHash).orElseThrow();
        if(!list.contains(key.getId())){
            throw ApplicationException.builder()
                    .message("Key Not Found")
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        this.keyRepository.delete(key);
    }

    private KeyDto handleReducedUrlInformations(Key keyEntity) {
        var reducedUrl = API_BASE_URL + keyEntity.getKeyHash();
        var calculateExpirationDate = keyEntity.getExpiresAt();
        return KeyDto.of(reducedUrl, calculateExpirationDate);
    }

    public ListSessionKeysResponse getAllKeysBySessionId(String sessionId) {
        if(sessionId == null){
            return ListSessionKeysResponse.from(sessionId, new ArrayList<>());
        }
        List<UUID> keysId = SessionService.returnAllKeysFromSession(sessionsRepository, sessionId);
        List<CompleteKeyResponse> keys = this.keyRepository.findAllByIdIn(keysId).stream().map(CompleteKeyResponse::build).collect(Collectors.toList());
        return ListSessionKeysResponse.from(sessionId, keys);
    }



}
