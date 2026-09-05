package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.checkout.CheckoutDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderstatus.UpdateOrderStatusRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.CheckoutMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.OrderMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.PaymentMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j // tạo logger
// ログ出力用アノテーション
@Service
// 注文関連サービス
@RequiredArgsConstructor
public class OrderService {

    // Repository層
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;


    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final StockRetryService stockRetryService;
    private final VNPayUtil vnPayUtil;



    public List<OrderDTO> getAllOrders() {

        // 全注文一覧取得
        return orderRepository.getAllOrders().stream()
                .map(order -> OrderMapper.toDTO(order))
                .toList();
    }

    public OrderDTO getOrderById(Long id) {

        //	findById(id) → Optional<Order>
        //	orElseThrow(...) → trích giá trị bên trong Optional
        //  Kết quả cuối cùng là: Order

        // 注文ID検索
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Mapper KHÔNG nên xử lý Optional
        // DTO変換
        return OrderMapper.toDTO(order);
    }

    public List<OrderDTO> getMyOrders() {

        // Lấy thông tin authentication hiện tại từ SecurityContext
        // Spring Security認証情報取得

        // (được set sau khi user login thành công qua Spring Security)
        // ログイン済みユーザー情報
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // check chưa đăng nhập hoặc authentication không hợp lệ
        // 未認証チェック
        if (auth == null || !auth.isAuthenticated()) {

            // ⚠️ nên dùng custom exception
            // カスタム例外推奨
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // username (principal) của user đang login
        // ログインユーザー名取得
        String username = auth.getName();

        // query DB để lấy user theo username
        // DBからユーザー取得

        // orElseThrow: nếu không tìm thấy → ném exception
        // 未存在時例外発生
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Viết gọn lại code dưới dùng Stream API
        // Stream API利用
        return orderRepository.findByUser(user).stream()
                .map(order -> OrderMapper.toDTO(order))
                .toList();
    }

    // ❌ KHÔNG @Transactional
    // Transactionなし

    // vì đã có transaction trong processStock
    // processStock側で管理
    public CheckoutDTO checkoutOrder(HttpServletRequest request) {

        // ============================================================
        // 1. GET CURRENT LOGGED-IN USER
        //    現在ログインユーザー取得
        // ============================================================

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));


        // ============================================================
        // 2. GET ACTIVE CART
        //    有効カート取得
        // ============================================================

        Cart cart = cartRepository.findByUserAndStatus(
                        user,
                        CartStatusCode.ACTIVE
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));


        // ============================================================
        // 3. CHECK EXISTING PENDING ORDER
        //    既存のPENDING注文確認
        //
        //    目的:
        //    - User chỉ được xử lý 1 Order PENDING tại một thời điểm
        //    - Nếu đã có Order PENDING thì không tạo Order mới
        //    - Kiểm tra Payment PENDING của Order đó
        // ============================================================

        Order pendingOrder = orderRepository
                .findByUserAndStatus(user, OrderStatusCode.PENDING)
                .orElse(null);

        // ------------------------------------------------------------
        // 3.1. CASE: PENDING ORDER EXISTS
        //      PENDING注文が存在する場合
        // ------------------------------------------------------------

        if (pendingOrder != null) {

            // Tìm Payment PENDING mới nhất của Order
            // Paymentが存在しない場合は処理を中断
            Payment pendingPayment = paymentRepository
                    .findFirstByOrderAndStatusOrderByCreatedAtDesc(
                            pendingOrder,
                            PaymentStatusCode.PENDING
                    )
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.PAYMENT_NOT_FOUND
                    ));


            // --------------------------------------------------------
            // 3.1.1. PAYMENT STILL VALID
            //       支払い有効期限内
            //
            //       Payment chắc chắn != null vì đã dùng orElseThrow()
            // --------------------------------------------------------

            LocalDateTime now = LocalDateTime.now();

            //expiredAt >= now → còn hạn

            if (pendingPayment.getExpiredAt().isAfter(now)
                    || pendingPayment.getExpiredAt().isEqual(now)) {

                // Payment vẫn còn hạn
                String paymentUrl = vnPayUtil.buildPaymentUrl(
                        pendingPayment.getAmount(),
                        pendingPayment.getTxnRef(),
                        request
                );

                return CheckoutMapper.toDTO(
                        pendingOrder,
                        PaymentMapper.toDTO(
                                pendingPayment,
                                paymentUrl
                        )
                );
            }


            // --------------------------------------------------------
            // 3.1.2. PAYMENT EXPIRED
            //       支払い有効期限切れ
            //
            //       Payment cũ không thể tiếp tục sử dụng
            //       → FAILED
            //       → Tạo Payment mới
            // --------------------------------------------------------

            //expiredAt < now → hết hạn

            if (pendingPayment.getExpiredAt().isBefore(now)) {

                // Đánh dấu Payment cũ đã hết hạn
                // 期限切れPaymentをFAILEDに変更
                pendingPayment.setStatus(PaymentStatusCode.FAILED);

                // Lưu trạng thái Payment mới vào database
                paymentRepository.save(pendingPayment);

                // Tạo Payment mới cho Order PENDING hiện tại
                // 新しいPaymentを作成
                PaymentDTO paymentDTO = paymentService.createPaymentUrl(
                        pendingOrder.getId(),
                        request
                );

                return CheckoutMapper.toDTO(
                        pendingOrder,
                        paymentDTO
                );
            }
        }


        // ============================================================
        // 4. VALIDATE CART
        //    カートバリデーション
        //
        //    CASE:
        //    Không có PENDING Order
        //    → Có thể bắt đầu tạo Order mới
        // ============================================================

        // Không cho phép checkout Cart rỗng
        // 空カートでのCheckoutを禁止
        if (cart.getCartItems().isEmpty()) {

            throw new BusinessException(ErrorCode.CART_EMPTY);
        }


        // ============================================================
        // 5. CREATE ORDER
        //    注文生成
        // ============================================================

        Order order = new Order();


        // ------------------------------------------------------------
        // 5.1. SET ORDER USER
        //      注文ユーザー設定
        // ------------------------------------------------------------

        order.setUser(user);


        // ------------------------------------------------------------
        // 5.2. LINK ORDER WITH CART
        //      OrderとCartを関連付け
        //
        //      Order.cart_id
        //      → このOrderがどのCartから作られたかを記録
        //
        //      Sau này có thể xác định chính xác:
        //      Order <-> Cart <-> CartItem
        // ------------------------------------------------------------

        order.setCart(cart);


        // ------------------------------------------------------------
        // 5.3. SET INITIAL ORDER STATUS
        //      注文初期ステータス設定
        // ------------------------------------------------------------

        order.setStatus(OrderStatusCode.PENDING);


        // ============================================================
        // 6. CREATE ORDER ITEMS FROM CART ITEMS
        //    カート商品から注文商品を生成
        //
        //    Mỗi CartItem
        //    → tạo một OrderItem
        // ============================================================

        for (CartItem cartItem : cart.getCartItems()) {


            // --------------------------------------------------------
            // 6.1. GET PRODUCT
            //      商品取得
            // --------------------------------------------------------

            Product product = cartItem.getProduct();


            // --------------------------------------------------------
            // 6.2. CREATE ORDER ITEM
            //      注文商品生成
            // --------------------------------------------------------

            OrderItem orderItem = new OrderItem();


            // --------------------------------------------------------
            // 6.3. SET PRODUCT
            //      商品設定
            // --------------------------------------------------------

            orderItem.setProduct(product);


            // --------------------------------------------------------
            // 6.4. SNAPSHOT PRODUCT NAME
            //      商品名をスナップショット保存
            //
            //      Lưu tên sản phẩm tại thời điểm checkout
            // --------------------------------------------------------

            orderItem.setProductName(product.getName());


            // --------------------------------------------------------
            // 6.5. SET QUANTITY
            //      数量設定
            // --------------------------------------------------------

            orderItem.setQuantity(cartItem.getQuantity());


            // --------------------------------------------------------
            // 6.6. SNAPSHOT PRODUCT PRICE
            //      商品価格をスナップショット保存
            //
            //      Lưu giá tại thời điểm tạo Order.
            //
            //      product.getPrice()
            //      → giá hiện tại của Product
            //
            //      OrderItem.price
            //      → giá snapshot của Order
            //
            //      Nếu sau này Product thay đổi giá,
            //      Order cũ vẫn giữ nguyên giá đã mua.
            // --------------------------------------------------------

            orderItem.setPrice(product.getPrice());


            // --------------------------------------------------------
            // 6.7. SAVE ORIGINAL CART ITEM ID
            //      元のCartItem IDを保存
            //
            //      Dùng để xác định:
            //      OrderItem này được tạo từ CartItem nào.
            // --------------------------------------------------------

            orderItem.setCartItemId(cartItem.getId());


            // --------------------------------------------------------
            // 6.8. ADD ORDER ITEM TO ORDER
            //      注文へ商品追加
            // --------------------------------------------------------

            order.addItem(orderItem);
        }


        // ============================================================
        // 7. RESERVE STOCK
        //    在庫予約
        //
        //    processStockWithRetry() bên trong đã xử lý:
        //
        //    availableStock = stock - reservedStock
        //
        //    Nếu đủ:
        //    → reservedStock tăng
        //
        //    Nếu không đủ:
        //    → throw exception
        // ============================================================

        stockRetryService.reserveStockWithRetry(order);


        // ============================================================
        // 8. SAVE ORDER
        //    注文保存
        //
        //    Lúc này Order đã có:
        //    - User
        //    - Cart
        //    - Status = PENDING
        //    - OrderItems
        //    - Product snapshot
        //    - Quantity
        //    - Price snapshot
        // ============================================================

        Order saveOrder = orderRepository.save(order);


        // ============================================================
        // 9. CREATE PAYMENT
        //    支払い生成
        //
        //    Tạo Payment:
        //    - Order ID
        //    - Amount
        //    - TxnRef
        //    - Status = PENDING
        //    - ExpiredAt
        //    - VNPay payment URL
        // ============================================================

        PaymentDTO paymentDTO = paymentService.createPaymentUrl(
                saveOrder.getId(),
                request
        );


        // ============================================================
        // 10. RETURN CHECKOUT DTO
        //     Checkout結果返却
        //
        //     Response gồm:
        //     - Order information
        //     - Payment information
        //     - VNPay payment URL
        // ============================================================

        return CheckoutMapper.toDTO(
                saveOrder,
                paymentDTO
        );
    }


    @Transactional
    // トランザクション制御
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    // 管理者またはスタッフのみ実行可能
    public OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequest rq) {

        //Tìm order by id
        // 注文取得
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        //Tim status by code
        // ステータスコード取得

        //query du lieu da duoc luu trong database
        // DB保存済みステータス

        //Chú ý:
        // 注意：

        // nếu viết order.getOrderStatus().setCode(rq.getStatusCode());
        // 直接変更禁止

        //Toàn bộ order đang PENDING → thành SHIPPED 😱 DAME!
        // 全注文ステータス破壊リスク

        OrderStatusCode newOrderStatus = rq.getStatusCode();

        //Status code hiện tại của order hiện tại
        // 現在ステータス取得
        if (order.getStatus() == null) {

            // ステータス未設定エラー
            throw new BusinessException(ErrorCode.ORDER_STATUS_MISSING);
        }

        OrderStatusCode currentOrderStatusCode = order.getStatus();

        //Không cập nhật nếu trùng statusCode
        // 同一ステータス更新禁止

        //Enum dùng == luôn cho nhanh
        // Enum比較は==使用可能
        if (currentOrderStatusCode == rq.getStatusCode()) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ALREADY_SET);
        }

        //Không thoả mãn điều kiện chỉ cập nhật from A to B thì throw new exception
        // 不正ステータス遷移チェック
        if (!isValidTransition(currentOrderStatusCode, rq.getStatusCode())) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        //Cap nhat status
        // ステータス更新

        // n order tham chieu den 1 status (status_id)
        // 複数注文が1ステータス参照

        //orderStatus se bao gom cac field dang ton tai trong class OrderStatus
        // OrderStatusエンティティ参照

        order.setStatus(newOrderStatus);

        // 保存してDTO返却
        return OrderMapper.toDTO(orderRepository.save(order));
    }

    //isValidTransition method
    // ステータス遷移可能判定

    //Cho biết từ trạng thái hiện tại có được phép chuyển sang trạng thái mới hay không
    // 現在状態から次状態へ変更可能か確認
    private boolean isValidTransition(OrderStatusCode from, OrderStatusCode to) {

        // map theo biến hằng số (constant field)
        // 定数Map参照

        // contains(to), xem "to" có thuộc Set.of() không?
        // 遷移可能状態チェック
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }


    //Dùng Set.of() thay vì List.of()
    // Set利用でcontains高速化

    //Không cần trùng lặp + cần check nhanh .contains()
    // 重複不要・検索高速
    private static final Map<OrderStatusCode, Set<OrderStatusCode>> ALLOWED = Map.of(

            // PENDING -> PAID / CANCELLED
            OrderStatusCode.PENDING,
            Set.of(OrderStatusCode.PAID, OrderStatusCode.CANCELLED),

            // PAID -> SHIPPED
            OrderStatusCode.PAID,
            Set.of(OrderStatusCode.SHIPPED),

            // SHIPPED -> DELIVERED
            OrderStatusCode.SHIPPED,
            Set.of(OrderStatusCode.DELIVERED),

            // 終了状態
            OrderStatusCode.DELIVERED,
            Set.of(),

            // キャンセル済み
            OrderStatusCode.CANCELLED,
            Set.of()
    );


    //@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    // orphanRemoval有効

    // có orphanRemoval nên đúng với method delete()
    // 子エンティティ自動削除
    public void deleteOrder(Long id) {

        // 注文削除
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        orderRepository.delete(order);
    }

    // Dòng này delete(): Chỉ xóa ở DB
    // DB削除のみ

    // KHÔNG tự cập nhật collection trong RAM
    // メモリコレクション未同期

    // orderItemRepository.delete(item);

    @Transactional

    // đảm bảo toàn bộ method chạy trong 1 transaction (ACID)
    // ACID保証

    // nếu có lỗi → rollback toàn bộ
    // エラー時全件ロールバック
    public void removeItemFromOrder(Long orderId, Long itemId) {

        // ログインユーザー取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // lấy Order từ DB theo id
        // 注文取得

        // nếu không tồn tại → ném exception
        // 未存在時例外
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // lấy OrderItem từ DB
        // 注文商品取得
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        // Check order cần remove item xem là:
        // 所有者チェック

        // user có trùng với user đang đăng nhập hiện tại không?
        // ログインユーザー一致確認
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // check item có thuộc order này không?
        // 注文所属チェック
        if (!item.getOrder().getId().equals(orderId)) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_NOT_BELONG_TO_ORDER);
        }

        // 🔥 dùng helper method trong Order (aggregate root)
        // Aggregate Root経由操作

        // thay vì thao tác trực tiếp vào collection
        // collection直接操作禁止

        // đảm bảo:
        // 整合性保証

        // - set quan hệ 2 chiều
        // 双方向関連維持

        // - cập nhật totalPrice
        // 合計金額更新

        // - giữ consistency
        // データ整合性維持
        order.removeItem(item);
    }





    public void paidOrder(Long orderId) {

        // 注文取得
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 重複コールバック防止
        if (order.getStatus() == OrderStatusCode.PAID) {

            // ログ出力
            log.info("Duplicate callback for order {}", orderId);

            return;
        }

        // ステータス変更
        changerStatus(order, OrderStatusCode.PAID);
    }

    private void changerStatus(Order order, OrderStatusCode newStatus) {

        // 現在状態取得
        OrderStatusCode currentStatusCode = order.getStatus();

        // 不正遷移チェック
        if (!isValidTransition(currentStatusCode, newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        // ステータス更新
        order.setStatus(newStatus);
    }

    public OrderDTO cancelOrder(Long id, Authentication authentication) {
        String username =
                authentication.getName();


        Order order =
                orderRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order not found"
                                )
                        );


        // kiểm tra chủ order
        if (!order.getUser()
                .getUsername()
                .equals(username)) {

            throw new RuntimeException(
                    "You cannot cancel this order"
            );
        }


        // chỉ cho cancel khi PENDING
        if (order.getStatus()
                != OrderStatusCode.PENDING) {

            throw new RuntimeException(
                    "Order cannot be cancelled"
            );
        }


        order.setStatus(
                OrderStatusCode.CANCELLED
        );


        Order saved =
                orderRepository.save(order);


        return OrderMapper.toDTO(saved);
    }
}

/*
Cart (ACTIVE)

↓

Checkout

↓

Create Order
status = PENDING

↓

Create Payment
status = PENDING

↓

Redirect VNPay

====================

VNPay

↓

IPN

↓

Verify SecureHash

↓

Payment SUCCESS ?
      │
 ┌────┴─────┐
 │          │
YES        NO
 │          │
 ▼          ▼

Payment    Payment
SUCCESS    FAILED

 │          │

Order      Order
PAID       CANCELLED

 │

Stock--

 │

Cart CHECKED_OUT

 │

Create New Cart

 │

Done



       // ===============================
        // 3. 🔥 TRỪ STOCK (có retry)
        // ===============================
        // 在庫減算（リトライ付き）

        // Tạo StockService để gọi proxy
        // Proxy経由呼び出し

        // Proxy : Một lớp trung gian do Spring tạo ra
        // Spring AOP Proxy

        // Phải đi qua proxy thì mới mở transaction
        // Proxy経由でTransaction有効
        processStockWithRetry(cart);




   Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE)
                .orElseGet(() -> {

                    // カート新規作成
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatusCode.ACTIVE)
                            .totalPrice(BigDecimal.ZERO)
                            .currency(CurrencyCode.VND)
                            .build();

                    return cartRepository.save(newCart);
                });

*/