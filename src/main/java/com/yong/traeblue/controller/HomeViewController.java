package com.yong.traeblue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {
    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
