package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Payment;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.PaymentService;
import com.chantaro.ecommerce.mini_ecommerce_backend.util.VNPayUtil;
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
    private final VNPayUtil vnPayUtil;

    // User redirect after payment
    @GetMapping("/return")
    public ResponseEntity<String> vnPayReturn(
            @RequestParam Map<String, String> params
    ) {

        if (!vnPayUtil.verify(params)) {
            return ResponseEntity.badRequest()
                    .body("Invalid signature");
        }

        if ("00".equals(params.get("vnp_ResponseCode"))
                &&
                "00".equals(params.get("vnp_TransactionStatus"))) {

            return ResponseEntity.ok("Payment success");
        }

        return ResponseEntity.badRequest()
                .body("Payment failed");
    }

    // VNPay server callback
    @GetMapping("/ipn")
    public ResponseEntity<String> vnPayIPN(
            @RequestParam Map<String, String> params
    ) {

        boolean success =
                paymentService.verify(params);


        if (!success) {

            return ResponseEntity
                    .badRequest()
                    .body("INVALID SIGNATURE");
        }


        paymentService.handleVNPayIPN(params);


        return ResponseEntity.ok("OK");
    }
}