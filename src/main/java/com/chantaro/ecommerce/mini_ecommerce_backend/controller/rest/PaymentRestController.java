package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

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

    // Create VNPay payment URL
    @PostMapping("/{orderId}/vnpay")
    public ResponseEntity<String> createPayment(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) {

        String paymentUrl =
                paymentService.createPaymentUrl(orderId, request);

        return ResponseEntity.ok(paymentUrl);
    }

    // User redirect after payment
    @GetMapping("/return")
    public ResponseEntity<String> vnPayReturn(
            @RequestParam Map<String, String> params
    ) {

        paymentService.handleVNPayIPN(params);

        return ResponseEntity.ok("Payment processed");
    }

    // VNPay server callback
    @GetMapping("/ipn")
    public ResponseEntity<String> vnPayIPN(
            @RequestParam Map<String, String> params
    ) {

        paymentService.handleVNPayIPN(params);

        return ResponseEntity.ok("OK");
    }
}