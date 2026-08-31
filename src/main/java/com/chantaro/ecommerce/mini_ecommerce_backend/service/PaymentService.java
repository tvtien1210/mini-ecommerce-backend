package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.PaymentDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    PaymentDTO createPaymentUrl(Long orderId, HttpServletRequest request);

    void handleVNPayIPN(Map<String, String> params);

    void handleExpiredPayments();

    boolean verify(Map<String, String> params);
}
