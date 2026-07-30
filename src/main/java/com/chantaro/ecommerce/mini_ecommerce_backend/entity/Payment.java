package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatusCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "payment")
public class Payment {

    // Primary key (auto increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết tới Order
    // 1 Order có thể có nhiều Payment (retry payment)
    // LAZY để tránh load order khi không cần
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Transaction Reference (mã tham chiếu giao dịch).
    // Transaction reference gửi sang cổng thanh toán (VD: VNPay)
    // KHÔNG phải orderId
    // unique để tránh duplicate
    // dùng để đối soát callback (IPN)
    @Column(name = "txn_ref", nullable = false, unique = true)
    private String txnRef;

    // Số tiền thanh toán
    // dùng BigDecimal để tránh sai số
    @Column(nullable = false)
    private BigDecimal amount;

    // Trạng thái payment
    // PENDING / SUCCESS / FAILED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatusCode status;

    // Thời điểm tạo payment
    // auto set khi insert DB
    // không cho update lại
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Thời điểm hết hạn thanh toán
    // KHÔNG dùng @CreationTimestamp
    // phải tự set bằng business logic (vd: +15 phút)
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Constructor dùng khi tạo payment mới
    public Payment(Order order, BigDecimal amount, PaymentStatusCode status) {
        this.order = order;
        this.amount = amount;
        this.status = status;

        // set luôn expire ( 15 phút)
        // this.expiredAt = LocalDateTime.now().plusMinutes(15);
    }
}