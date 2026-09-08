package com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentPaymentDTO {
    private OrderDTO order;
    private Long paymentId;
    private PaymentStatusCode paymentStatusCode;
}
