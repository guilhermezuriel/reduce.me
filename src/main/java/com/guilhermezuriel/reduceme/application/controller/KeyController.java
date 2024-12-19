package com.guilhermezuriel.reduceme.application.controller;

import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.repository.KeyRepository;
import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("key/")
@RequiredArgsConstructor
public class KeyController {

    private final KeyGenerationService keyGenerationService;

    @PostMapping("create")
    public ResponseEntity<Void> createKey(@RequestBody String url) {
       this.keyGenerationService.generateKeys(url);
        return ResponseEntity.ok().build();
    }
}
