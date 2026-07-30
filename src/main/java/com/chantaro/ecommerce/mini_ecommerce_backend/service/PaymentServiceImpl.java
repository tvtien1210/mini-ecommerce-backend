package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.PaymentMapper;
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

@Service
@RequiredArgsConstructor //Không phải tạo Constructor (DI) thủ công
// コンストラクタを手動作成せずにDIを行う
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final VNPayUtil vnPayUtil;

    // 1. CREATE PAYMENT
    // 1. 決済URL作成
    @Override
    @Transactional //Nếu save Payment thành công nhưng build URL lỗi thì: rollback
    public PaymentDTO createPaymentUrl(Long orderId,
                                   HttpServletRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatusCode.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_SUCCESS);
        }

        // tạo txnRef DUY NHẤT
        // 一意なtxnRefを生成
        String newTxnRef = "ORDER_" + orderId + "_" + System.currentTimeMillis();

        Payment savedPayment = new Payment();
        savedPayment.setOrder(order);
        savedPayment.setTxnRef(newTxnRef);
        savedPayment.setAmount(order.getTotalPrice());
        savedPayment.setStatus(PaymentStatusCode.PENDING);
        savedPayment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        paymentRepository.save(savedPayment);

        // truyền txnRef vào build URL
        // txnRefを決済URL生成に渡す

        String paymentUrl = vnPayUtil.buildPaymentUrl(savedPayment.getAmount(), savedPayment.getTxnRef(), request);


        return PaymentMapper.toDTO(savedPayment,paymentUrl);
    }

    // 2. HANDLE IPN
    // 2. IPN処理
    @Override
    @Transactional
    public void handleVNPayIPN(Map<String, String> params) {

        // 1. verify hash
        // 1. ハッシュ署名検証
        if (!vnPayUtil.verify(params)) {
            throw new BusinessException(ErrorCode.INVALID_SIGNATURE);
        }

        // txnRef
        // 決済トランザクション参照ID
        String txnRef = params.get("vnp_txnRef");

        Payment payment = paymentRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_SIGNATURE));

        Order order = payment.getOrder();

        // 2. idempotent, nếu payment này đã xử lý SUCCESS rồi thì thôi, dừng method tại đây
        // 2. 冪等性保証:
        // 既にSUCCESS処理済みなら何もしない
        if (payment.getStatus() == PaymentStatusCode.SUCCESS) {

            // thoát khỏi method NGAY LẬP TỨC
            // メソッドを即終了
            return;
        }

        // 3. VERIFY AMOUNT
        // 3. 金額検証

        // VNPay amount gửi về
        // VNPayから返却された金額
        long vnpAmount = Long.parseLong(params.get("vnp_Amount"));

        // expectedAmount = amount * 100,
        // la gia tri trong db sau khi *100,
        // giong voi value ma vnp request
        //
        // DBの金額を100倍した値
        // VNPayのリクエスト金額フォーマットと合わせる
        long expectedAmount = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))

                // bỏ chữ số sau dấu phẩy
                // 小数点以下切り捨て
                .longValue();

        if (vnpAmount != expectedAmount) {

            throw new BusinessException(ErrorCode.INVALID_PAYMENT_AMOUNT);

        }

        // 4. success
        // 4. 決済成功
        if ("00".equals(params.get("vnp_ResponseCode"))) {

            payment.setStatus(PaymentStatusCode.SUCCESS);
            order.setStatus(OrderStatusCode.PAID);

            // trừ stock thật
            // 実在庫を減算
            for (OrderItem item : order.getOrderItems()) {

                Product product = item.getProduct();

                product.setStock(product.getStock() - item.getQuantity());

                product.setReservedStock(
                        product.getReservedStock() - item.getQuantity()
                );
            }

        } else {

            // fail
            // 決済失敗
            payment.setStatus(PaymentStatusCode.FAILED);

            handlePaymentFailed(order);
        }
    }

    // 3. PAYMENT FAILED
    // 3. 決済失敗処理
    @Override
    public void handlePaymentFailed(Order order) {

        order.setStatus(OrderStatusCode.CANCELLED);

        // release reserve stock
        // Giải phóng số lượng hàng đã giữ trước
        //
        // 予約在庫を解放
        for (OrderItem item : order.getOrderItems()) {

            Product product = item.getProduct();

            product.setReservedStock(
                    product.getReservedStock() - item.getQuantity()
            );
        }
    }

    // 4. HANDLE TIMEOUT
    // 4. タイムアウト決済処理
    @Override
    @Transactional
    public void handleExpiredPayments() {

        // Tìm tất cả payment:
        // - đang ở trạng thái PENDING
        // - và đã hết hạn (expiredAt < thời gian hiện tại)
        //
        // 以下条件の決済を検索:
        // - PENDING状態
        // - 有効期限切れ
        List<Payment> expiredPayments =
                paymentRepository.findByStatusAndExpiredAtBefore(
                        PaymentStatusCode.PENDING,
                        LocalDateTime.now()
                );

        // Duyệt từng payment hết hạn
        // 期限切れ決済を1件ずつ処理
        for (Payment payment : expiredPayments) {

            // Lấy order tương ứng của payment hiện tại
            // 1 order có thể có nhiều payment attempt
            //
            // 現在のpaymentに紐づくorderを取得
            // 1つのorderに複数回の決済試行が存在可能
            Order order = payment.getOrder();

            // Nếu order đã PAID rồi thì bỏ qua
            // Tránh trường hợp:
            // - payment cũ bị expired
            // - nhưng payment retry mới đã thanh toán thành công
            // => không được overwrite order thành failed/cancelled
            //
            // orderが既にPAIDならスキップ
            // 古い決済はexpiredでも、
            // リトライ決済が成功している可能性があるため
            // 状態を上書きしない
            if (order.getStatus() == OrderStatusCode.PAID) {
                continue;
            }

            // Xử lý payment thất bại:
            // ví dụ:
            // - update order status
            // - release reserved stock
            // - gửi notification
            //
            // 決済失敗処理:
            // - 注文状態更新
            // - 予約在庫解放
            // - 通知送信など
            handlePaymentFailed(order);
        }
    }

    @Override
    public boolean verify(Map<String, String> params) {
        return vnPayUtil.verify(params);
    }

}