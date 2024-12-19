package com.guilhermezuriel.reduceme.application.services.keygen;

import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            e.printStackTrace();
            urlHashBytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        }
        urlHashBytes = Base64.getEncoder().encode(urlHashBytes);

        //TODO: RANDOOM 7 STRINGS FROM URLHASHBYTES AND STORE THEM, IF IT EXISTS RANDOOM AGAIN
        var key = "key";
        this.keyRepository.existsKeyByKey(key);

    }
}
