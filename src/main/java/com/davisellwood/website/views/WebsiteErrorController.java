package com.davisellwood.website.views;

import static com.davisellwood.website.common.WebsiteConstants.ERROR_404_PAGE;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebsiteErrorController implements ErrorController {
    @RequestMapping("/error")
    public String errorPage(HttpServletRequest request) {
        return ERROR_404_PAGE;
    }
}
