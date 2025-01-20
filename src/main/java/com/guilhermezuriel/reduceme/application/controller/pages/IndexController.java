package com.guilhermezuriel.reduceme.application.controller.pages;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
