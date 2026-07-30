package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.checkout.CheckoutDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Payment;

public class CheckoutMapper {
    public static CheckoutDTO toDTO(Order order, PaymentDTO payment, String paymentUrl) {
        return new CheckoutDTO(
                order.getId(),
                payment.getPaymentId(), order.getStatus(),
                payment.getPaymentStatusCode(),
                payment.getPaymentUrl()
        );
    }
}
