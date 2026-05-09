package com.chantaro.ecommerce.mini_ecommerce_backend.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    private Long categoryId;
    private String name;
    private String description;
    //Them price vao rq nhung ben DTO se khong nhin thay price
    private BigDecimal price;
    private Integer stock;
}

        /*"name": "iPhone 13",
        "description": "iPhone 13 128GB, chip A15 Bionic",
        "stock": 30,*/