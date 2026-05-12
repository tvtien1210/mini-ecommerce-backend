package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderstatus.UpdateOrderStatusRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.*;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CartStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.OrderMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j // tạo logger
@Service
public class OrderService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    @Autowired
    public OrderService(CartRepository cartRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserRepository userRepository, ProductRepository productRepository, StockService stockService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
    }


    public List<OrderDTO> getAllOrders() {

        return orderRepository.findAll().stream().map(order -> OrderMapper.toDTO(order)).toList();
    }

    public OrderDTO getOrderById(Long id) {
        //	findById(id) → Optional<Order>
        //	orElseThrow(...) → trích giá trị bên trong Optional
        //  Kết quả cuối cùng là: Order
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found by id"));
        // Mapper KHÔNG nên xử lý Optional
        return OrderMapper.toDTO(order);

        /* return OrderMapper.toDTO(orderRepository.findById(id))->Sai kiểu
        findById() KHÔNG trả về Order, nó trả về Optional<Order>
        Cách fix: // Mapper KHÔNG nên xử lý Optional, mapper xử lý Order order

        Or làm theo cách sau
        return orderRepository.findById(id)
                .map(order -> OrderMapper.toDTO(order))
                .orElseThrow(() -> new RuntimeException("Order not found"));*/
    }


//    @Transactional
//    public OrderDTO createOrder(CreateOrderRequest rq) {
//
//        // ===== 1. Lấy user đang login =====
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Not found user by username: " + username));
//
//        // ===== 2. Tạo Order =====
//        Order order = new Order();
//
//        // set user cho order
//        order.setUser(user);
//
//
//        // dùng bảng order_status
//        OrderStatus pending = orderStatusRepository.findByCode(OrderStatusCode.PENDING).orElseThrow(() -> new RuntimeException("Status not found"));
//
//        // set status cho order
//        order.setOrderStatus(pending);
//
//
//        // QUAN TRỌNG 🍺
//
//        // ===== 3. Loop items =====
//        for (CreateOrderItemRequest orderItemRequest : rq.getOrderItems()) {
//
//            // 3.1 Lấy product
//            Product product = productRepository.findById(orderItemRequest.getProductId()).orElseThrow(() -> new RuntimeException("Not found product by id:" + orderItemRequest.getProductId()));
//
//            // 3.2 Check stock
//            if (product.getStock() < orderItemRequest.getQuantity()) {
//                throw new RuntimeException("Out of stock");
//            }
//
//            // 3.3 Trừ stock
//            product.setStock(product.getStock() - orderItemRequest.getQuantity());
//
//
//            // 3.4 Tạo OrderItem
//            OrderItem item = new OrderItem();
//
//
//            // set product
//            item.setProduct(product);
//
//
//            // set quantity
//            item.setQuantity(orderItemRequest.getQuantity());
//
//
//            // set price
//            item.setPrice(product.getPrice()); //snapshot price từ product gốc tại thời điểm click, không thay đổi nếu bị cập nhật mới
//
//
//            // 3.5 Add vào Order dùng Helper
//            order.addItem(item);
//
//        }
//
//        // ===== 6. Return DTO =====
//        return OrderMapper.toDTO(orderRepository.save(order));
//    }


    public List<OrderDTO> getMyOrders() {

        // Lấy thông tin authentication hiện tại từ SecurityContext
        // (được set sau khi user login thành công qua Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // check chưa đăng nhập hoặc authentication không hợp lệ
        if (auth == null || !auth.isAuthenticated()) {
            // ⚠️ nên dùng custom exception (vd: UnauthorizedException)
            // thay vì RuntimeException
            throw new RuntimeException("Unauthenticated");
        }

        // username (principal) của user đang login
        String username = auth.getName();

        // query DB để lấy user theo username
        // orElseThrow: nếu không tìm thấy → ném exception
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found by username: " + username));

        // Viết gọn lại code dưới dùng Stream API, phải tạo list orders trước bằng orderRepository.findByUser(user)
        return orderRepository.findByUser(user).stream().map(order -> OrderMapper.toDTO(order)).toList();
    }

    // Một user (or một user.id) có nhiều orders
//        // → query tất cả orders thuộc về user này
//        List<Order>code orders = orderRepository.findByUser(user);
//
//        // chuẩn bị list kết quả trả về cho API
//        List<OrderDTO> result = new ArrayList<>();
//
//        // loop từng order để convert sang DTO
//        for (Order order : orders) {
//
//            // map entity → DTO (tránh expose entity ra ngoài API)
//            // DTO giúp:
//            // - bảo mật dữ liệu (không lộ field nhạy cảm)
//            // - control response format
//            OrderDTO dto = OrderMapper.toDTO(order);
//
//            result.add(dto);
//        }
//
//        // return danh sách order của user hiện tại
//        return result;

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public OrderDTO updateOrderStatus(Long id, UpdateOrderStatusRequest rq) {

        //Tìm order by id
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found order"));

        //Tim status by code (query du lieu da duoc luu trong database)
        //Chú ý: nếu viết order.getOrderStatus().setCode(rq.getStatusCode()); //Toàn bộ order đang PENDING → thành SHIPPED 😱 DAME!
        OrderStatusCode newOrderStatus = rq.getStatusCode();

        //Status code hiện tại của order hiện tại VD:
        if (order.getStatus() == null) {
            throw new RuntimeException("Current order has no status");
        }
        OrderStatusCode currentOrderStatusCode = order.getStatus();

        //Không cập nhật nếu trùng statusCode
        //Enum dùng == luôn cho nhanh
        if (currentOrderStatusCode == rq.getStatusCode()) {
            throw new RuntimeException("The status is already the same.");
        }


        //Không thoả mãn điều kiện chỉ cập nhật from A to B thì throw new exception
        if (!isValidTransition(currentOrderStatusCode, rq.getStatusCode())) {
            throw new RuntimeException("Invalid status transition from " + currentOrderStatusCode + " to " + rq.getStatusCode());
        }


        //Cap nhat status, n order tham chieu den 1 status (status_id), giong nhu n order tham chieu den 1 user (user_id)
        //orderStatus se bao gom cac field dang ton tai trong class OrderStatus, vi du code + name
        order.setStatus(newOrderStatus);
        return OrderMapper.toDTO(orderRepository.save(order));
    }

    //isValidTransition method, Cho biết từ trạng thái hiện tại có được phép chuyển sang trạng thái mới hay không

//    private boolean isValidTransition (String from, String to){
//        if (from.equals("PENDING") && to.equals("PAID")) return true;
//        if (from.equals("PENDING") && to.equals("CANCELLED")) return true;
//        if (from.equals("PAID") && to.equals("SHIPPED")) return true;
//        if (from.equals("PAID") && to.equals("CANCELLED")) return true;
//        if (from.equals("SHIPPED") && to.equals("DELIVERED")) return true;
//        if (from.equals("DELIVERED") && to.equals("STOP")) return true;
//        if (from.equals("CANCELLED") && to.equals("STOP")) return true;

//        return false;
//    }


    //isValidTransition method, Cho biết từ trạng thái hiện tại có được phép chuyển sang trạng thái mới hay không
    private boolean isValidTransition(OrderStatusCode from, OrderStatusCode to) {
        // map theo biến hằng số (constant field) ALLOWED được tạo bởi method Map.of() phía dưới
        // from: gán luôn "PENDING", Set.of() gán luôn PAID và CANCELLED theo kiểu dữ liệu đã gọi,
        // sau đó là contains(to), xem "to" (là đối số truyền vào vd: "PAID" xem có thuộc Set.of() không?)
        // nếu contains -> return true;
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }


    //Dùng Set.of() thay vì List.of() vì:
    //Không cần trùng lặp + cần check nhanh .contains()
    private static final Map<OrderStatusCode, Set<OrderStatusCode>> ALLOWED = Map.of(
            OrderStatusCode.PENDING, Set.of(OrderStatusCode.PAID, OrderStatusCode.CANCELLED),
            OrderStatusCode.PAID, Set.of(OrderStatusCode.SHIPPED),
            OrderStatusCode.SHIPPED, Set.of(OrderStatusCode.DELIVERED),
            OrderStatusCode.DELIVERED, Set.of(),
            OrderStatusCode.CANCELLED, Set.of()
    );


    //@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true), có orphanRemoval nên đúng với method delete()
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found order by id = " + id));
        orderRepository.delete(order);
    }

    // Dòng này delete(): Chỉ xóa ở DB (khi flush/commit), KHÔNG tự cập nhật collection trong RAM
    // orderItemRepository.delete(item);

    @Transactional
    // đảm bảo toàn bộ method chạy trong 1 transaction (ACID)
    // nếu có lỗi → rollback toàn bộ
    public void removeItemFromOrder(Long orderId, Long itemId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // lấy Order từ DB theo id
        // nếu không tồn tại → ném exception
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // lấy OrderItem từ DB
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // Check order cần remove item xem là: user có trùng với user đang đăng nhập hiện tại không?
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("403 Forbidden : Access deny");
        }

        // check item có thuộc order này không?
        if (!item.getOrder().getId().equals(orderId)) {
            throw new RuntimeException("Item does not belong to this order");
        }

        // 🔥 dùng helper method trong Order (aggregate root)
        // thay vì thao tác trực tiếp vào collection
        // đảm bảo:
        // - set quan hệ 2 chiều (order <-> orderItem)
        // - cập nhật totalPrice (nếu có)
        // - giữ consistency (tính nhất quán dữ liệu)
        order.removeItem(item);
    }

    // ❌ KHÔNG @Transactional -> vì đã có transaction trong processStock
    public OrderDTO checkoutOrder() {
        // ===============================
        // 1. Lấy user hiện tại
        // ===============================
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Not found user"));

        // ===============================
        // 2. Lấy cart ACTIVE
        // ===============================
        // Cart cart = ...
        // nếu null → throw
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .status(CartStatusCode.ACTIVE)
                            .totalPrice(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });

        // ===============================
        // 3. Validate cart
        // ===============================
        // cart.getItems().isEmpty() → throw
        // chặn không cho check out nếu Cart vẫn: status = ACTIVE, nhưng cartItems = []
        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // ===============================
        // 3. 🔥 TRỪ STOCK (có retry)
        // ===============================
        // Tạo StockService để gọi proxy
        // Proxy : Một lớp trung gian do Spring tạo ra để chặn lời gọi method cùng class và thêm logic (transaction, security, log…)
        // Phải đi qua proxy thì mới mở transaction
        processStockWithRetry(cart);


        // ===============================
        // 4. Tạo Order : CHECK OUT ORDER 🍺
        // ===============================
        // Order order = new Order();
        // order.setUser(user);
        // order.setCreatedAt(LocalDateTime.now());
        Order order = new Order();
        order.setUser(user);
        //createdAt tao tu dong o entity roi ma?

        // set status = PENDING
        // OrderStatus pending = ...
        // order.setStatus(pending);
        order.setStatus(OrderStatusCode.PENDING);

        // ===============================
        // 5. Loop cart items (Lặp qua từng phần)
        // ===============================
        // BigDecimal total = BigDecimal.ZERO;

        // for (CartItem cartItem : cart.getItems()) {
        for (CartItem cartItem : cart.getCartItems()) {

            // 5.1 Lấy product
            // Product product = ...
            Product product = cartItem.getProduct();

            // 5.4 Tạo OrderItem
            // OrderItem item = new OrderItem();
            OrderItem orderItem = new OrderItem();

            // set order
            // order đã tìm theo user đang đăng nhập,
            // có orders.id = bao nhiêu,
            // thì sẽ tạo item.order_id fk tương ứng ở item table
            // orderItem.setOrder(order); // đối chiếu với addItem method trong order entity, done!

            // set product
            orderItem.setProduct(product);

            // set quantity
            orderItem.setQuantity(cartItem.getQuantity());

            // ⚠️ SNAPSHOT PRICE
            orderItem.setPrice(product.getPrice());

            // add list orderItem vừa tìm được vào order
            // order.getItems().add(item);
            order.addItem(orderItem);
        }

        // ===============================
        // 7. Save order vào db
        // ===============================
        Order saveOrder = orderRepository.save(order);

        // ===============================
        // 8. Set Status become Checked_out   -> snapshot lịch sử
        // ===============================
        // cart.getItems().clear();
        cart.setStatus(CartStatusCode.CHECKED_OUT);
        // tạo mới cart
        Cart newCart = Cart.builder()
                .user(user)
                .status(CartStatusCode.ACTIVE)
                .totalPrice(BigDecimal.ZERO)
                .build();

        cartRepository.save(newCart);

        // ===============================
        // 9. Return DTO
        // ===============================
        return OrderMapper.toDTO(saveOrder);
    }


    //Xử lý trừ tồn kho với cơ chế retry khi xảy ra optimistic locking
    public void processStockWithRetry(Cart cart) {

        int maxRetry = 3;   // Số lần retry tối đa
        int attempt = 0;    // Số lần đã thử

        // Loop retry
        while (attempt < maxRetry) {
            try {
                // Gọi logic chính xử lý stock (có thể throw exception)
                stockService.processStock(cart);

                return; // Nếu thành công thì thoát luôn, không retry nữa

            } catch (ObjectOptimisticLockingFailureException e) {
                // Exception này xảy ra khi:
                // Có 2 transaction cùng update 1 record (stock)
                // -> version bị lệch (optimistic lock fail)

                attempt++; // Tăng số lần thử

                // Nếu đã retry quá số lần cho phép
                if (attempt >= maxRetry) {
                    // Ném lỗi ra ngoài (fail hẳn)
                    throw new RuntimeException("System busy, please try again");
                }

                try {
                    // ⏳ Delay 1 chút trước khi retry
                    // Tránh retry liên tục gây xung đột tiếp
                    Thread.sleep(100);

                } catch (InterruptedException ex) {
                    // Nếu thread bị interrupt thì set lại trạng thái interrupt
                    //interrupt = ngắt / làm gián đoạn thread
                    Thread.currentThread().interrupt();
                    //OK, tao bị interrupt → tao không xử lý nữa, nhưng tao ghi nhớ trạng thái bị interrupt
                }
            }
        }
    }


    public void paidOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatusCode.PAID) {
            log.info("Duplicate callback for order {}", orderId);
            return;
        }
        changerStatus(order, OrderStatusCode.PAID);
    }

    private void changerStatus(Order order, OrderStatusCode newStatus) {
        OrderStatusCode currentStatusCode = order.getStatus();
        if (!isValidTransition(currentStatusCode, newStatus)) {
            throw new RuntimeException("Invalid transition from " + currentStatusCode + "to " + newStatus);
        }
        order.setStatus(newStatus);
    }
}


/*
User click "Pay"
        ↓
Backend tạo payment URL (có hash)
        ↓
Redirect sang VNPay
        ↓
User thanh toán
        ↓
VNPay gọi ipnUrl (server bạn)  ✅ QUAN TRỌNG
        ↓
Bạn verify hash
        ↓
Update order = PAID
        ↓
VNPay redirect user về returnUrl
        ↓
Frontend hiển thị kết quả
*/



