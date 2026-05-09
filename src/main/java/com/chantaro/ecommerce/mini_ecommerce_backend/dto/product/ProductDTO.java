package com.chantaro.ecommerce.mini_ecommerce_backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
