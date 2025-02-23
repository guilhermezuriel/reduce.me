package com.guilhermezuriel.reduceme.application.services.keygen;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.config.exceptions.ApplicationException;
import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import com.guilhermezuriel.reduceme.application.services.keygen.dto.KeyDto;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyGenerationService {

    private final KeyRepository keyRepository;

    @Value("${reduceme.api_url_base}")
    private String API_BASE_URL;

    public KeyDto generateKeys(CreateReducedUrlForm form) {
        var isValidUrl = Utils.isValidUrl(form.url());
        if(Boolean.FALSE.equals(isValidUrl)){
            throw new RuntimeException("Invalid URL");
        }
        String keyHash = Utils.digestUrl(form.url());
        Key keyEntity =  Key.builder()
                .id(UUID.randomUUID())
                .originalUrl(form.url())
                .keyHash(keyHash)
                .expiresAt(LocalDateTime.now().plusSeconds(80))
                .build();
        this.keyRepository.insert(keyEntity);
        return this.handleReducedUrlInformations(keyEntity);
    }

    public String getKeyByKeyHash(String keyHash) {
        Optional<Key> key = this.keyRepository.findKeyByKeyHash(keyHash);
        if(key.isEmpty()){
            throw ApplicationException.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Key Not Found")
                    .build();
        }
        if(key.get().getExpiresAt().isBefore(LocalDateTime.now())){
            throw ApplicationException.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Key expired").build();
        }
        return key.get().getOriginalUrl();
    }

    private KeyDto handleReducedUrlInformations(Key keyEntity) {
        var reducedUrl = API_BASE_URL + keyEntity.getKeyHash();
        var calculateExpirationDate = keyEntity.getExpiresAt();
        return KeyDto.of(reducedUrl, calculateExpirationDate);
    }



}
