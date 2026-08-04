package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentRestController {

    private final PaymentService paymentService;

    @PostMapping("/create/")
    public PaymentDTO vnPayCreate(Long orderId, HttpServletRequest request) {
        PaymentDTO paymentUrl = paymentService.createPaymentUrl(orderId, request);
        return paymentUrl;

    }

    @GetMapping("/return")
    public String vnPayReturn(
            @RequestParam Map<String, String> params
    ) {

        System.out.println("===== VNPay RETURN =====");

        params.forEach((key, value) ->
                System.out.println(key + " = " + value)
        );


        // Kiểm tra chữ ký VNPay
        if (!paymentService.verify(params)) {

            return "Invalid VNPay signature";
        }


        // Kiểm tra kết quả thanh toán
        if ("00".equals(params.get("vnp_ResponseCode"))) {

            return "Payment success";
        }


        return "Payment failed";
    }

    // VNPay server callback
    @GetMapping("/ipn")
    public String vnPayIPN(@RequestParam Map<String, String> params) {

        paymentService.handleVNPayIPN(params);

        return "OK";
    }
}