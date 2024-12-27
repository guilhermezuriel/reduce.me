package com.guilhermezuriel.reduceme.application.services.keygen;

import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
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

        var key_hash = Arrays.toString(urlHashBytes).substring(0, 7);
        boolean existsKey = this.keyRepository.existsKeyByKey_hash(key_hash);

        if(existsKey){
            return;
        }

        var entity = Key.builder()
                .key_hash(key_hash)
                .build();

        this.keyRepository.insert(entity);
    }

}
