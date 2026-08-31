package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.category.CategoryDTO;

import com.chantaro.ecommerce.mini_ecommerce_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryRestController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }
}