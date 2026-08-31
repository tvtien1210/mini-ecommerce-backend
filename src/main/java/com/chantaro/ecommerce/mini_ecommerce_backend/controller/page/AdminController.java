package com.chantaro.ecommerce.mini_ecommerce_backend.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    @GetMapping("/admin")
    public String dashboard(){
        return "admin-dashboard";
    }

    @GetMapping("/admin/products")
    public String productsPage() {
        return "admin-manage-products";
    }
}


