package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Payment;

public class PaymentMapper {
    public static PaymentDTO toDTO(Payment payment, String paymentUrl) {
        return new PaymentDTO(
                payment.getId(),
                payment.getStatus(),
                paymentUrl
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