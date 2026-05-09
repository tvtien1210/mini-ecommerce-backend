package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderitem.OrderItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;

import java.util.List;
public class OrderMapper {
    // Convert từ Entity Order -> OrderResponse (DTO trả về cho client)

    public static OrderDTO toDTO(Order order) {
        // Lấy danh sách OrderItem (order.getItems()) từ Order (Entity)
        // -> convert từng orderItem sang OrderItemDTO
        List<OrderItemDTO> orderItems = order.getOrderItems().stream().map(orderItem -> OrderItemMapper.toDTO(orderItem)).toList();
        // Tạo object OrderResponse để trả về
        return new OrderDTO(
                order.getId(),
                orderItems,
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
