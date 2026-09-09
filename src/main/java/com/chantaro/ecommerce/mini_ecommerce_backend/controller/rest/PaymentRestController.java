package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.CurrentPaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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


    //Tạm thời không dùng vì returnUrl: https://mini-ecommerce-backend-production-69d1.up.railway.app/payment-result
    //đã redirect trực tiếp về payment-result.html có js gọi về api/payment/txnref để authentication token và check payment.status rồi

//    @GetMapping("/return")
//    public String vnPayReturn(
//            @RequestParam Map<String, String> params
//    ) {
//
//        System.out.println("===== VNPay RETURN =====");
//
//        params.forEach((key, value) ->
//                System.out.println(key + " = " + value)
//        );
//
//
//        // Kiểm tra chữ ký VNPay
//        if (!paymentService.verify(params)) {
//
//            return "Invalid VNPay signature";
//        }
//
//
//        // Kiểm tra kết quả thanh toán
//        if ("00".equals(params.get("vnp_ResponseCode"))) {
//
//            return "Payment success";
//        }
//
//
//        return "Payment failed";
//    }

    // VNPay server callback
    @GetMapping("/ipn")
    public String vnPayIPN(@RequestParam Map<String, String> params) {

        paymentService.handleVNPayIPN(params);

        return "OK";
    }

    //Find payment with txnRef
    @GetMapping("/{txnRef}")
    public CurrentPaymentDTO getPaymentByTxnRef(@PathVariable String txnRef) {
        return paymentService.getPaymentByTxnRef(txnRef);
    }
}