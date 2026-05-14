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

    // 1. CREATE PAYMENT
    @Override
    public String createPaymentUrl(Long orderId,
                                   HttpServletRequest request) {

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));


        // tạo txnRef DUY NHẤT
        String txnRef = UUID.randomUUID().toString();

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTxnRef(txnRef);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        paymentRepository.save(payment);

        // truyền txnRef vào build URL
        return vnPayUtil.buildPaymentUrl(
                order,
                txnRef,
                request
        );
    }

    // 2. HANDLE IPN
    @Override
    @Transactional
    public void handleVNPayIPN(Map<String, String> params) {

        // 1. verify hash
        if (!vnPayUtil.verify(params)) {
            throw new RuntimeException("Invalid signature");
        }

        //txnRef
        String txnRef = params.get("vnp_txnRef");

        Payment payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Invalid signature"));

        Order order = payment.getOrder();

        // 2. idempotent, nếu payment này đã xử lý SUCCESS rồi thì thôi, dừng method tại đây
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // thoát khỏi method NGAY LẬP TỨC
            return;
        }

        // 3. VERIFY AMOUNT

        // VNPay amount gửi về
        long vnpAmount = Long.parseLong(params.get("vnp_Amount"));

        //expectedAmount = amount * 100, la gia tri trong db sau khi *100, giong voi value ma vnp request

        long expectedAmount = payment.getAmount()

                .multiply(BigDecimal.valueOf(100))

                //bỏ chữ số sau dấu phẩy
                .longValue();

        if (vnpAmount != expectedAmount) {

            throw new RuntimeException("Invalid payment amount");

        }

        // 4. success
        if ("00".equals(params.get("vnp_ResponseCode"))) {

            payment.setStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatusCode.PAID);

            // trừ stock thật
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();

                product.setStock(product.getStock() - item.getQuantity());
                product.setReservedStock(product.getReservedStock() - item.getQuantity());
            }

        } else {
            // fail
            payment.setStatus(PaymentStatus.FAILED);
            handlePaymentFailed(order);
        }
    }

    // 3. PAYMENT FAILED

    @Override
    public void handlePaymentFailed(Order order) {
        order.setStatus(OrderStatusCode.CANCELLED);
        //release reserve stock
        //Giải phóng số lượng hàng đã giữ trước
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setReservedStock(product.getReservedStock() - item.getQuantity());
        }
    }

    // 4. HANDLE TIMEOUT

    @Override
    @Transactional
    public void handleExpiredPayments() {

        // Tìm tất cả payment:
        // - đang ở trạng thái PENDING
        // - và đã hết hạn (expiredAt < thời gian hiện tại)
        List<Payment> expiredPayments =
                paymentRepository.findByStatusAndExpiredAtBefore(
                        PaymentStatus.PENDING,
                        LocalDateTime.now()
                );

        // Duyệt từng payment hết hạn
        for (Payment payment : expiredPayments) {

            // Lấy order tương ứng của payment hiện tại
            // 1 order có thể có nhiều payment attempt
            Order order = payment.getOrder();

            // Nếu order đã PAID rồi thì bỏ qua
            // Tránh trường hợp:
            // - payment cũ bị expired
            // - nhưng payment retry mới đã thanh toán thành công
            // => không được overwrite order thành failed/cancelled
            if (order.getStatus() == OrderStatusCode.PAID) {
                continue;
            }

            // Xử lý payment thất bại:
            // ví dụ:
            // - update order status
            // - release reserved stock
            // - gửi notification
            handlePaymentFailed(order);
        }
    }


}
