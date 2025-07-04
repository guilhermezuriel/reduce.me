package com.guilhermezuriel.reduceme.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/session")
public class SessionController {

    @PostMapping("/generate")
    public ResponseEntity<String> createSession() {
        String sessionId = UUID.randomUUID().toString();
        return ResponseEntity.ok(sessionId);
    }

//    @GetMapping("/sessions/keys")
//    public String keys(@RequestHeader("X-Session-Id") String sessionId, Model model) {
//        ListSessionKeysResponse response = this.keyGenerationService.getAllKeysBySessionId(sessionId);
//        model.addAttribute("urls", response.keys());
//        return "index";
//    }
}
