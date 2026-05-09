package com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderstatus;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    private OrderStatusCode statusCode;
}
