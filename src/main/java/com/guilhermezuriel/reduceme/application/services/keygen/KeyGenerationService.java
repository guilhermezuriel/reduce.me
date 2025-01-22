package com.guilhermezuriel.reduceme.application.services.keygen;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeyGenerationService {

    private final KeyRepository keyRepository;

    public Key generateKeys(CreateReducedUrlForm form) {
        byte[] urlHashBytes;
        var isValidUrl = Utils.isValidUrl(form.url());

        if(Boolean.FALSE.equals(isValidUrl)){
            throw new RuntimeException("Invalid URL");
        }

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD-5");
            urlHashBytes = algorithm.digest(form.url().getBytes(StandardCharsets.UTF_8));
        }catch (NoSuchAlgorithmException e){
            urlHashBytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        }
        urlHashBytes = Base64.getEncoder().encode(urlHashBytes);

        var keyHash = new String(urlHashBytes).substring(0, 7);
        Key key = this.keyRepository.insert(new Key(UUID.randomUUID(), keyHash, form.url()));

        return key;
    }

    public String getKeyByKeyHash(String keyHash) {
        Optional<Key> key = this.keyRepository.findKeyByKeyHash(keyHash);
        if(key.isEmpty()){
            throw new IllegalArgumentException("Key not found");
        }
        return key.get().getOriginalUrl();
    }
}
