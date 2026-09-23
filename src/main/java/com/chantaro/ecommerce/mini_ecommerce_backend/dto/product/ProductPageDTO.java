package com.chantaro.ecommerce.mini_ecommerce_backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageDTO {
    private List<ProductDTO> products;

    private int currentPage;

    private int totalPages;

    private long totalProducts;
}
