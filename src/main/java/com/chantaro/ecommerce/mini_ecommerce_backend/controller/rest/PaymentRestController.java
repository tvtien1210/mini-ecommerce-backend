package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
// Đánh dấu đây là REST API controller
// Spring sẽ tự động trả JSON/text thay vì trả về HTML page

@RequestMapping("/api/payments")

public class PaymentRestController {

    private final PaymentService paymentService;

    // Constructor Injection
    // Spring tự inject PaymentService vào controller
    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/vnpay/ipn")
    // API nhận callback/IPN từ VNPay
    // Full URL:
    // /api/payments/vnpay/ipn

    public ResponseEntity<?> vnPayIPN(

            // Lấy toàn bộ request params VNPay gửi về và convert thành:
            // Map<String, String>
            // Ví dụ:
            // vnp_Amount=1000000
            // vnp_ResponseCode=00
            // =>
            // {
            //   "vnp_Amount" : "1000000",
            //   "vnp_ResponseCode" : "00"
            // }
            @RequestParam Map<String, String> params
    ) {

        // Gọi service xử lý logic:
        paymentService.handleVNPayIPN(params);

        // Trả response về cho VNPay
        return ResponseEntity.ok("success");
    }
}
