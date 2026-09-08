package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.CurrentPaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Payment;

public class PaymentMapper {
    public static PaymentDTO toDTO(Payment payment, String paymentUrl) {
        return new PaymentDTO(
                payment.getId(),
                payment.getStatus(),
                paymentUrl
        );
    }

    public static CurrentPaymentDTO currentPaymentDTO(Payment payment, OrderDTO order) {
        return new CurrentPaymentDTO(
                order,
                payment.getId(),
                payment.getStatus()
        );
    }
}

/*
* public class PaymentMapper {
    public PaymentDTO toDTO(Payment payment, String paymentUrl) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getId());
        dto.setPaymentUrl(paymentUrl);
        return dto;
    }
}*/