package com.guilhermezuriel.reduceme.application.services.keygen;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import com.guilhermezuriel.reduceme.application.services.keygen.dto.KeyDto;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyGenerationService {

    private final KeyRepository keyRepository;

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
                .build();
        this.keyRepository.insert(keyEntity);
        return KeyDto.of(keyEntity);
    }

    public String getKeyByKeyHash(String keyHash) {
        Optional<Key> key = this.keyRepository.findKeyByKeyHash(keyHash);
        if(key.isEmpty()){
            throw new IllegalArgumentException("Key not found");
        }
        return key.get().getOriginalUrl();
    }


}
