package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController

@RequestMapping("/api/payment")

public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {

        this.paymentService = paymentService;

    }

    // URL VNPay redirect user về sau khi thanh toán

    // Chỉ dùng để hiển thị kết quả

    @GetMapping("/vnpay-return")

    public ResponseEntity<?> vnPayReturn(

            @RequestParam Map<String, String> params

    ) {

        // check chữ ký

        boolean valid = paymentService.verify(params);

        if (!valid) {

            return ResponseEntity.badRequest()

                    .body("Invalid signature");

        }

        String responseCode = params.get("vnp_ResponseCode");

        // thanh toán thành công

        if ("00".equals(responseCode)) {

            return ResponseEntity.ok(

                    "Thanh toán thành công"

            );

        }

        // thanh toán thất bại

        return ResponseEntity.ok(

                "Thanh toán thất bại"

        );

    }

    // VNPay server gọi vào đây để xác nhận payment

    // Đây mới là nơi update DB

    @GetMapping("/vnpay-ipn")

    public ResponseEntity<?> vnPayIPN(

            @RequestParam Map<String, String> params

    ) {

        try {

            // xử lý callback

            paymentService.handleVNPayIPN(params);

            return ResponseEntity.ok(

                    Map.of(

                            "RspCode", "00",

                            "Message", "Confirm Success"

                    )

            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()

                    .body(

                            Map.of(

                                    "RspCode", "99",

                                    "Message", "Unknown error"

                            )

                    );

        }

    }

}
