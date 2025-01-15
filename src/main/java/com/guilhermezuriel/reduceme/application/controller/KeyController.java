package com.guilhermezuriel.reduceme.application.controller;

import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.services.KeyGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1")
public class KeyController {

    private KeyGenerationService keyGenerationService;

    public KeyController(KeyGenerationService keyGenerationService) {
        this.keyGenerationService = keyGenerationService;
    }

    @PostMapping("key/create")
    public ResponseEntity<Void> createKey(@RequestBody String url) {
       this.keyGenerationService.generateKeys(url);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{keyHash}")
    public ResponseEntity<Key> getKey(@PathVariable String keyHash) {
        var key = this.keyGenerationService.getKeyByKeyHash(keyHash);
        return ResponseEntity.ok().body(key);
    }
}
