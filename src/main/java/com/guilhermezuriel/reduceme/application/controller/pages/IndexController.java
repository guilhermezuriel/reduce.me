package com.guilhermezuriel.reduceme.application.controller.pages;

import com.guilhermezuriel.reduceme.application.Utils;
import com.guilhermezuriel.reduceme.application.services.keygen.KeyGenerationService;
import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class IndexController {

    private final KeyGenerationService keyGenerationService;

    @Value("${reduceme.api_url_base}")
    private String API_URL_BASE;

    @GetMapping("/home")
    public String indexHtml(Model model) {
        return "index";
    }

    @PostMapping("/home")
    public String createKey(CreateReducedUrlForm form, Model model) {
        var isValidUrl = Utils.isValidUrl(form.url());
        if (!isValidUrl) {
            return "redirect:/home";
        }
        var key = this.keyGenerationService.generateKeys(form);
        var newUrlForm = new CreateReducedUrlForm(key.reducedUrl());
        model.addAttribute("form", newUrlForm);
        return "copyKey";
    }
}
