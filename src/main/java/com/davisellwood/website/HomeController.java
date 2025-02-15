package com.davisellwood.website;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@EnableCaching
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {
        // TODO: update homepage to use dynamic work links
        return "home";
    }
}
