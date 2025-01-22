package com.guilhermezuriel.reduceme.application.controller.pages;

import com.guilhermezuriel.reduceme.application.services.keygen.form.CreateReducedUrlForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/")
public class IndexController {

    @GetMapping("/")
    public String indexHtml(Model model) {
        model.addAttribute("url");
        return "index";
    }

    @PostMapping("/")
    public String createKey(CreateReducedUrlForm form, Model model) {
        System.out.println(form);
        model.addAttribute("form", form);
        return "copyKey";
    }
}
