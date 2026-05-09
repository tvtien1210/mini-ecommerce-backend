package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderitem.OrderItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.OrderItem;

public class OrderItemMapper {
    // Convert từ Entity OrderItem -> OrderItemResponse (DTO)
    public static OrderItemDTO toDTO (OrderItem orderItem){
        return new OrderItemDTO(
                orderItem.getProduct().getId(),         //id sản phẩm
                orderItem.getProduct().getName(),                //tên sản phẩm
                orderItem.getQuantity(),                         //số lượng mua
                orderItem.getPrice()                             //giá tại thời điểm đặt hàng (!)
        );
    }
}
