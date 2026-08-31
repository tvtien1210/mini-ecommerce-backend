package com.chantaro.ecommerce.mini_ecommerce_backend.dto.product;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private CurrencyCode currency;
    private Integer stock;
    private String imageUrl;
    private Integer categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
