package com.guilhermezuriel.reduceme.application.controller.pages;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.config.infra.InfraProperties;
import com.guilhermezuriel.reduceme.application.model.Sessions;
import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import com.guilhermezuriel.reduceme.application.services.sessions.SessionService;
import com.guilhermezuriel.reduceme.application.services.sessions.dto.ListSessionKeysResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final KeyGenerationService keyGenerationService;
    private final SessionService sessionService;

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String indexHtml(@CookieValue(name = "session_id", required = false) String sessionId,
                            HttpServletResponse response,
                            Model model) {
        Sessions sessions = sessionService.manageSession(sessionId, response);
        ListSessionKeysResponse urls = this.keyGenerationService.getAllKeysBySessionId(sessions.getSessionId());
        model.addAttribute("urls", urls.keys());
        model.addAttribute("form", new CreateReducedUrlForm());
        return "index";
    }

    @PostMapping("/home")
    public String createKey(@CookieValue(name = "session_id", required = true) String sessionId,
                            @ModelAttribute CreateReducedUrlForm form,
                            Model model) {
        var isValidUrl = Utils.isValidUrl(form.url());
        if (!isValidUrl) {
            return "redirect:/home";
        }
        this.keyGenerationService.generateKeys(form, sessionId);
        model.addAttribute("form", new CreateReducedUrlForm());
        model.addAttribute("urls", this.keyGenerationService.getAllKeysBySessionId(sessionId).keys());
        return "redirect:/home";
    }

}
