package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart.CartDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CartItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;

import java.util.List;

public class CartMapper {

    public static CartDTO toDTO(Cart cart) {

        // Lấy danh sách CartItem từ Cart
        // cartItem là từng phần tử bên trong cart.getCartItems()
        List<CartItemDTO> cartItems = cart.getCartItems()
                .stream()

                // Chuyển từng CartItem Entity → CartItemDTO
                .map(cartItem -> CartItemMapper.toDTO(cartItem))

                // Gom tất cả CartItemDTO thành một List
                .toList();


        // Tạo và trả về CartDTO
        return new CartDTO(

                // ID của Cart
                cart.getId(),

                // Danh sách CartItemDTO
                cartItems,

                // Trạng thái của Cart
                // Ví dụ: ACTIVE
                cart.getStatus(),

                // Tổng tiền của Cart
                cart.getTotalPrice(),

                // Loại tiền tệ
                // Ví dụ: VND
                cart.getCurrency(),

                // Thời gian tạo Cart
                cart.getCreatedAt(),

                // Thời gian cập nhật Cart gần nhất
                cart.getUpdatedAt()
        );
    }

}
