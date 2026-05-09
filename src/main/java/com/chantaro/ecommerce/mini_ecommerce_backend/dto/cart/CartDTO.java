package com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CartItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CartStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long id;
    private List<CartItemDTO> cartItems;
    private CartStatusCode cartStatusCode;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
