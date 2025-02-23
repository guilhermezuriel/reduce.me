package com.guilhermezuriel.reduceme.application.controller;

import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import com.guilhermezuriel.reduceme.application.services.keygen.dto.KeyDto;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class KeyController {

    @Value("${reduceme.api_url_base}")
    private String API_BASE_URL;

    private final KeyGenerationService keyGenerationService;

    @PostMapping(value = "/key/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyDto> createKey(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key);
    }

    @PostMapping(value = "/key/create", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createKeyv2(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key.toString());
    }

    @PostMapping(value = "/key/create", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> createKeyv3(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key.toString());
    }

    @GetMapping("/{keyHash}")
    public RedirectView getKey(@PathVariable String keyHash) {
        RedirectView redirectView = new RedirectView();
        try {
            var original_url = this.keyGenerationService.getKeyByKeyHash(keyHash);
            redirectView.setUrl(original_url);
        } catch (Exception e) {
           redirectView.setUrl(API_BASE_URL + "/home");
        }
        return redirectView;
    }
}
