package com.guilhermezuriel.reduceme.application.controller;

import com.guilhermezuriel.reduceme.application.config.infra.InfraProperties;
import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import com.guilhermezuriel.reduceme.application.services.keygen.dto.KeyDto;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class KeyController {

    private final KeyGenerationService keyGenerationService;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyDto> createKey(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key);
    }

    @PostMapping(value = "/create", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createKeyv2(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key.toString());
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> createKeyv3(@RequestBody CreateReducedUrlForm form) {
        var key =  this.keyGenerationService.generateKeys(form);
        return ResponseEntity.ok().body(key.toString());
    }

    @GetMapping("/r/{keyHash}")
    public RedirectView getKey(@PathVariable String keyHash) {
        RedirectView redirectView = new RedirectView();
        try {
            var original_url = this.keyGenerationService.getKeyByKeyHash(keyHash);
            redirectView.setUrl(original_url);
        } catch (Exception e) {
            redirectView.setUrl(InfraProperties.getSTATIC_API_URL_BASE() + "home");
        }
        return redirectView;
    }
    @DeleteMapping("/keys/delete/{keyHash}")
    public ResponseEntity<String> deleteKey(@CookieValue(name = "session_id", required = true) String sessionId,
                            @PathVariable("keyHash") String keyHash) {
        this.keyGenerationService.deleteKeyByKeyHash(sessionId, keyHash);
        return ResponseEntity.ok().build();
    }

}
