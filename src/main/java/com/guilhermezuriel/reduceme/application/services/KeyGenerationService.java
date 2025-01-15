package com.guilhermezuriel.reduceme.application.services;

import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyGenerationService {

    private final KeyRepository keyRepository;

    public void generateKeys(String url) {
        byte[] urlHashBytes;
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD-5");
            urlHashBytes = algorithm.digest(url.getBytes(StandardCharsets.UTF_8));
        }catch (NoSuchAlgorithmException e){
            urlHashBytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        }
        urlHashBytes = Base64.getEncoder().encode(urlHashBytes);

        var keyHash = Arrays.toString(urlHashBytes).substring(0, 7);
        boolean existsKey = this.keyRepository.existsKeyByKeyHash(keyHash);

        if(existsKey){
            return;
        }

        var entity = new Key();
        entity.setKeyHash(keyHash);
        entity.setOriginalUrl(url);

        this.keyRepository.insert(entity);
    }

    public Key getKeyByKeyHash(String keyHash) {
        Optional<Key> key = this.keyRepository.findKeyByKeyHash(keyHash);
        if(key.isPresent()){
            return key.get();
        }
        throw new RuntimeException("Key not found");
    }
}
