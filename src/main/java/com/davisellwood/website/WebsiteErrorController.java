package com.davisellwood.website;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebsiteErrorController implements ErrorController {
    @RequestMapping("/error")
    public String errorPage(HttpServletRequest request) {
        return "error";
    }
}
