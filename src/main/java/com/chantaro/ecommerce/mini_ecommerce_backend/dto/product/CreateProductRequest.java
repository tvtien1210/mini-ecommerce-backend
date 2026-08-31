package com.chantaro.ecommerce.mini_ecommerce_backend.dto.product;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CurrencyCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    //Them price vao rq nhung ben DTO se khong nhin thay price
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Currency is required")
    private CurrencyCode currency;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock must be greater than or equal to 0"
    )
    private Integer stock;

    //@NotBlank(message = "Image URL is required")
    private String imageUrl;
}

        /*"name": "iPhone 13",
        "description": "iPhone 13 128GB, chip A15 Bionic",
        "stock": 30,*/