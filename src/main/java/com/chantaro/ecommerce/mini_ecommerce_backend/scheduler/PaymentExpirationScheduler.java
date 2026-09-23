package com.chantaro.ecommerce.mini_ecommerce_backend.scheduler;

import com.chantaro.ecommerce.mini_ecommerce_backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentExpirationScheduler {

    // PaymentServiceを注入
    // Dùng PaymentService để xử lý payment hết hạn
    private final PaymentService paymentService;

    // 60秒ごとに自動実行
    // Tự động chạy mỗi 60 giây
    @Scheduled(fixedDelay = 60_000)
    public void handleExpiredPayments() {

        System.out.println("PaymentExpirationScheduler is running..." );

        // 期限切れ決済を処理
        // Gọi PaymentService để tìm và xử lý các payment đã hết hạn qua handleExpiredPayments()
        paymentService.handleExpiredPayments();
    }
}
