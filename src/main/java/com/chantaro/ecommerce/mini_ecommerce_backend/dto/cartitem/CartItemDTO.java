package com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;          // cartItemId
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;

}
