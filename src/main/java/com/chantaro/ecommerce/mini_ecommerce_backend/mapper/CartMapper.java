package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart.CartDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CartItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;

import java.util.List;

public class CartMapper {
    public static CartDTO toDTO(Cart cart) {
        //cartItem (không có s, không phải cartItems mà là cartItem) chính là từng phần tử bên trong cart.getCartItems()
        List<CartItemDTO> cartItems = cart.getCartItems().stream().map(cartItem -> CartItemMapper.toDTO(cartItem)).toList();
        return new CartDTO(
                cart.getId(),
                cartItems,
                cart.getStatus(),
                cart.getTotalPrice(),
                cart.getCurrency(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }


}
