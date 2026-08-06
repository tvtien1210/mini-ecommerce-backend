package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment.PaymentDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.checkout.CheckoutDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderstatus.UpdateOrderStatusRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.CheckoutMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.OrderMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j // tạo logger
// ログ出力用アノテーション
@Service
// 注文関連サービス
public class OrderService {

    // Repository層
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final PaymentRepository paymentRepository;
    private final PaymentServiceImpl paymentServiceImpl;

    private final VNPayUtil vnPayUtil;



    @Autowired
    public OrderService(CartRepository cartRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserRepository userRepository, ProductRepository productRepository, StockService stockService, PaymentRepository paymentRepository, PaymentServiceImpl paymentServiceImpl, VNPayUtil vnPayUtil) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
        this.paymentRepository = paymentRepository;
        this.paymentServiceImpl = paymentServiceImpl;
        this.vnPayUtil = vnPayUtil;
    }



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

        // ===============================
        // 1. Lấy user hiện tại
        // ===============================
        // 現在ログインユーザー取得

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // ===============================
        // 2. Lấy cart ACTIVE
        // ===============================
        // 有効カート取得

        Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        // ===============================
        // 3. Validate cart
        // ===============================
        // カートバリデーション

        // cart.getItems().isEmpty() → throw
        // 空カート禁止

        // chặn không cho check out nếu Cart vẫn: status = ACTIVE, nhưng cartItems = []
        // 商品未存在チェック
        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }


        // ===============================
        // 4. Tạo Order : CHECK OUT ORDER 🍺
        // ===============================
        // 注文生成

        Order order = new Order();


        order.setUser(user);

        //Tạo cart_id trong order table, để xác định order_id <-> cart_id tham chiếu chính xác với nhau
        //khi xoá sản phẩm -> check out sp nào thì chỉ xoá sp đã check out đó
        order.setCart(cart);

        // Set status = PENDING
        // 初期ステータス設定
        order.setStatus(OrderStatusCode.PENDING);

        // ===============================
        // 5. Loop cart items
        // ===============================
        // カート商品ループ

        for (CartItem cartItem : cart.getCartItems()) {

            // 5.1 Lấy product
            // 商品取得
            Product product = cartItem.getProduct();

            // 5.4 Tạo OrderItem
            // 注文商品生成
            OrderItem orderItem = new OrderItem();

            // set product
            // 商品設定
            orderItem.setProduct(product);

            // set name
            orderItem.setProductName(product.getName());

            // set quantity
            // 数量設定
            orderItem.setQuantity(cartItem.getQuantity());

            // ⚠️ SNAPSHOT PRICE
            // スナップショット価格保存
            orderItem.setPrice(product.getPrice()); // đây là giá snapshot thời điểm đặt hàng cart.getProduct(), theo code tạo object này  Product product = cartItem.getProduct();


            // Lưu lại cart_item gốc(order nào quản lý cart đó)
            orderItem.setCartItemId(cartItem.getId());


            // add list orderItem vừa tìm được vào order
            // 注文へ商品追加
            // order.getItems().add(item);
            order.addItem(orderItem);
        }

        // ===============================
        // 7. Save order vào db
        // ===============================
        // 注文保存
        Order saveOrder = orderRepository.save(order);


        //Trong processStockWithRetry đã có xử lý reserverStock
        processStockWithRetry(order);


        //Tạo Payment - Status Pending
        PaymentDTO paymentDTO = paymentServiceImpl.createPaymentUrl(saveOrder.getId(), request);


        // ===============================
        // 9. Return DTO
        // ===============================
        // DTO返却
        return CheckoutMapper.toDTO(saveOrder,paymentDTO);
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


    //Xử lý trừ tồn kho với cơ chế retry khi xảy ra optimistic locking
    // 楽観ロック失敗時リトライ処理
    public void processStockWithRetry(Order order) {

        int maxRetry = 3;   // Số lần retry tối đa
        // 最大リトライ回数

        int attempt = 0;    // Số lần đã thử
        // 現在試行回数

        // Loop retry
        // リトライループ
        while (attempt < maxRetry) {
            try {

                // Gọi logic chính xử lý stock
                // 在庫処理実行

                // (có thể throw exception)
                // 例外発生可能
                stockService.reserveStock(order);

                return; // Nếu thành công thì thoát luôn
                // 成功時終了

            } catch (ObjectOptimisticLockingFailureException e) {

                // Exception này xảy ra khi:
                // 楽観ロック例外

                // Có 2 transaction cùng update 1 record
                // 同時更新競合

                // -> version bị lệch
                // version不一致

                attempt++; // Tăng số lần thử
                // リトライ回数加算

                // Nếu đã retry quá số lần cho phép
                // 最大回数超過
                if (attempt >= maxRetry) {

                    // Ném lỗi ra ngoài
                    // 業務例外送出
                    throw new BusinessException(ErrorCode.SYSTEM_BUSY);
                }

                try {

                    // ⏳ Delay 1 chút trước khi retry
                    // リトライ前待機

                    // Tránh retry liên tục gây xung đột tiếp
                    // 競合緩和
                    Thread.sleep(100);

                } catch (InterruptedException ex) {

                    // Nếu thread bị interrupt
                    // Thread割り込み検知

                    //interrupt = ngắt / làm gián đoạn thread
                    // スレッド中断

                    Thread.currentThread().interrupt();

                    //OK, tao bị interrupt
                    // 割り込み状態保持
                }
            }
        }
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