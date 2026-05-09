package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatus;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.OrderRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.PaymentRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.ProductRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor //Không phải tạo Constructor (DI) thủ công
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final VNPayUtil vnPayUtil;

    // =========================
    // 1. CREATE PAYMENT
    // =========================
    @Override
    public String createPaymentUrl(Long orderId,
                                   HttpServletRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        // tạo txnRef DUY NHẤT
        String txnRef = UUID.randomUUID().toString();

        Payment payment = new Payment();

        payment.setOrder(order);

        // save txnRef vào DB
        payment.setTxnRef(txnRef);

        payment.setAmount(
                order.getTotalPrice()
                        .multiply(new BigDecimal(100))
        );

        payment.setStatus(PaymentStatus.PENDING);

        payment.setCreatedAt(LocalDateTime.now());

        payment.setExpiredAt(
                LocalDateTime.now().plusMinutes(15)
        );

        paymentRepository.save(payment);

        // truyền txnRef vào build URL
        return vnPayUtil.buildPaymentUrl(
                order,
                txnRef,
                request
        );
    }

    // =========================
    // 2. HANDLE IPN
    // =========================
    @Override
    @Transactional
    public void handleVNPayIPN(Map<String, String> params) {

        // 1. verify hash
        if (!vnPayUtil.verify(params)) {
            throw new RuntimeException("Invalid signature");
        }

        Long orderId = Long.parseLong(params.get("vnp_TxnRef"));
        Order order = orderRepository.findById(orderId).orElseThrow();

        Payment payment = paymentRepository.findByTxnRef(String.valueOf(orderId))
                .orElseThrow();

        // 2. idempotent
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        // 3. success
        if ("00".equals(params.get("vnp_ResponseCode"))) {

            payment.setStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatusCode.PAID);

            // ✅ trừ stock thật
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();

                product.setStock(product.getStock() - item.getQuantity());
                product.setReservedStock(product.getReservedStock() - item.getQuantity());
            }

        } else {
            // ❌ fail
            handlePaymentFailed(order);
        }
    }

    // =========================
    // 3. PAYMENT FAILED
    // =========================
    @Override
    public void handlePaymentFailed(Order order) {

        order.setStatus(OrderStatusCode.CANCELLED);

        // 🔥 release reserved stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();

            product.setReservedStock(product.getReservedStock() - item.getQuantity());
        }
    }

    // =========================
    // 4. HANDLE TIMEOUT
    // =========================
    @Override
    @Transactional
    public void handleExpiredPayments() {

        List<Payment> expiredPayments =
                paymentRepository.findByStatusAndExpiredAtBefore(
                        PaymentStatus.PENDING,
                        LocalDateTime.now()
                );

        for (Payment payment : expiredPayments) {

            payment.setStatus(PaymentStatus.EXPIRED);

            Order order = payment.getOrder();

            // ❗ tránh overwrite nếu đã paid
            if (order.getStatus() == OrderStatusCode.PAID) {
                continue;
            }

            handlePaymentFailed(order);
        }
    }
}
