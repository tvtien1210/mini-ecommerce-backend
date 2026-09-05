package com.chantaro.ecommerce.mini_ecommerce_backend.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("activePage","home");

        return "home";
    }

    @GetMapping("/cart")
    public String cart() {

        return "cart";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/myorders")
    public String myorders() {
        return "myorders";
    }
}
