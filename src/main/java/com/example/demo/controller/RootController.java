package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    /**
     * Root endpoint — tells users to use the frontend.
     * Also prevents Spring Security from saving "/?continue" as a redirect target.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:http://localhost:3000";
    }
}
