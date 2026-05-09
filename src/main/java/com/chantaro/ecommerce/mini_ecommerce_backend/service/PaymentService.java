package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    String createPaymentUrl(Long orderId, HttpServletRequest request);

    void handleVNPayIPN(Map<String, String> params);

    void handlePaymentFailed(Order order);

    void handleExpiredPayments();
}
