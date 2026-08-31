package com.chantaro.ecommerce.mini_ecommerce_backend.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class PageController {
    @GetMapping("/403")
    public String accessDenied(){
        return "403";
    }
}
