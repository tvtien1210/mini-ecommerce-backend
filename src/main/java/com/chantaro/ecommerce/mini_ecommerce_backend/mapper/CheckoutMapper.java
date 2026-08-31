package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.checkout.CheckoutDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;

public class CheckoutMapper {
    public static CheckoutDTO toDTO(Order order, PaymentDTO paymentDTO) {
        return new CheckoutDTO(
                order.getId(),
                paymentDTO.getPaymentId(),
                order.getStatus(),
                paymentDTO.getPaymentStatusCode(),
                paymentDTO.getPaymentUrl()
        );
    }
}
