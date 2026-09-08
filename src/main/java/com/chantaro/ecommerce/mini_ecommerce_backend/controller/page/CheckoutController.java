package com.chantaro.ecommerce.mini_ecommerce_backend.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CheckoutController {
    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }
}
