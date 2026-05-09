package com.chantaro.ecommerce.mini_ecommerce_backend.dto.order;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderitem.OrderItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id; //id order này tự nhảy số thứ tự
    //private Long userId;
    private List<OrderItemDTO> orderItems;
    private OrderStatusCode status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
