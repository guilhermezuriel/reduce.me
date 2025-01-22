package com.guilhermezuriel.reduceme.application.controller;

import com.guilhermezuriel.reduceme.application.model.Key;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping
public class KeyController {

    private KeyGenerationService keyGenerationService;

    public KeyController(KeyGenerationService keyGenerationService) {
        this.keyGenerationService = keyGenerationService;
    }

    @PostMapping("/key/create")
    public ResponseEntity<Key> createKey(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key);
    }

    @GetMapping("/{keyHash}")
    public RedirectView getKey(@PathVariable String keyHash) {
        var original_url = this.keyGenerationService.getKeyByKeyHash(keyHash);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(original_url);
        return redirectView;
    }
}
