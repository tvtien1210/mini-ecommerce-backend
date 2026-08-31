package com.chantaro.ecommerce.mini_ecommerce_backend.service;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.category.CategoryDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName()
                ))
                .toList();
    }
}