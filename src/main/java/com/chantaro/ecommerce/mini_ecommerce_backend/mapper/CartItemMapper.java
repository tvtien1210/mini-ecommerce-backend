package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CartItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.CartItem;

public class CartItemMapper {
    public static CartItemDTO toDTO(CartItem cartItem){
        return new CartItemDTO(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }
}
