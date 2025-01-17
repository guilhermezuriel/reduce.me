package com.guilhermezuriel.reduceme.application.services;

import com.datastax.oss.driver.api.core.CqlSession;
import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
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

    public Key generateKeys(CreateReducedUrlForm form) {
        byte[] urlHashBytes;
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

    public Key getKeyByKeyHash(String keyHash) {
        Optional<Key> key = this.keyRepository.findKeyByKeyHash(keyHash);
        if(key.isPresent()){
            return key.get();
        }
        throw new RuntimeException("Key not found");
    }
}
