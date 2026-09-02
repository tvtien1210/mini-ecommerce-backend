package com.chantaro.ecommerce.mini_ecommerce_backend.controller.page;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.category.CategoryDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoriesViewController {


    @GetMapping("/categories")
    public String showCategories(Model model) {

        model.addAttribute("activePage", "categories");

        return "categories";
    }
}
