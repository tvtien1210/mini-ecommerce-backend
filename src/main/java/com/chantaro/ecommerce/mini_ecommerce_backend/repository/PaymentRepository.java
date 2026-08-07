package com.chantaro.ecommerce.mini_ecommerce_backend.repository;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Payment;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // tìm theo txnRef (dùng trong IPN)
    Optional<Payment> findByTxnRef(String txnRef);

    // tìm payment của 1 order (nếu sau này support retry payment)
    List<Payment> findByOrderId(Long orderId);

    // dùng cho cron job (tác vụ theo lịch trình): tìm payment hết hạn
    List<Payment> findByStatusAndExpiredAtBefore(
            PaymentStatusCode status,
            LocalDateTime time
    );

    // check duplicate (idempotent nâng cao)
    boolean existsByTxnRef(String txnRef);

    //Tìm Payment còn PENDING, lấy Payment PENDING mới nhất của Order
    Optional<Payment> findFirstByOrderAndStatusOrderByCreatedAtDesc(Order order, PaymentStatusCode statusCode);

}
