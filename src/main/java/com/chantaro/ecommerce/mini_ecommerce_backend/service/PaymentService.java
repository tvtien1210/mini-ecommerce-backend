package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Payment;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    PaymentDTO createPaymentUrl(Long orderId, HttpServletRequest request);

    void handleVNPayIPN(Map<String, String> params);

    void handleExpiredPayments();

    boolean verify(Map<String, String> params);
}
