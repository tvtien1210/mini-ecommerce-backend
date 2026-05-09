package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor //Không phải tạo Constructor (DI) thủ công
public class PaymentServiceImplPractice implements PaymentService {

    //1.Create payment
    @Override
    public String createPaymentUrl(Long orderId, HttpServletRequest request) {
        return null;
    }
}
