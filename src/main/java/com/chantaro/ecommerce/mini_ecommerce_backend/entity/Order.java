package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity // Đánh dấu đây là entity map với bảng "orders"
@Table(name = "orders") // Tên bảng trong DB
public class Order {

    // ================== PRIMARY KEY ==================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Setter(AccessLevel.NONE) // không cho set id từ bên ngoài
    private Long id;

    // ================== USER ==================
    // Nhiều Order thuộc về 1 User
    // LAZY → chỉ load user khi gọi order.getUser()
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // khóa ngoại
    private User user;

    @OneToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    // ================== ORDER STATUS ==================
    // Nhiều Order có cùng một trạng thái (pending, shipped,...)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatusCode status;

    // ================== TOTAL PRICE ==================
    @NotNull // không được null
    @PositiveOrZero // phải >= 0
    @Setter(AccessLevel.NONE)
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    // precision = 12 → tối đa 12 chữ số
    // scale = 2 → 2 chữ số sau dấu phẩy (vd: 9999999999.99)
    private BigDecimal totalPrice;

    // ================== ORDER ITEMS ==================
    @Setter(AccessLevel.NONE)
    //orphanRemoval = true nghĩa là:
    //Khi item bị remove khỏi collection, Hibernate sẽ xóa row trong DB
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    //cascade = CascadeType.ALL : set quan hệ 2 chiều, order luu orderItem, va orderItem cung se luu order
    //new HashSet<>(); Khởi tạo collection items rỗng cho Order, !=null: khác null, giả sử items ban đầu là null thì order.getItems().add(item); sẽ NullPointerException
    //new HashSet<>(); đảm bảo order.getItems().add(item); luôn chạy được
    //HashSet? Không cho phép trùng item,Phù hợp với quan hệ 1-N
    private Set<OrderItem> orderItems = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // ================== CREATED TIME ==================
    @Setter(AccessLevel.NONE) // không cho set tay
    @CreationTimestamp // Hibernate tự set khi insert
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ================== UPDATED TIME ==================
    @Setter(AccessLevel.NONE) // không cho set tay
    @UpdateTimestamp // Hibernate tự update khi update record
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================== CONSTRUCTOR ==================
    public Order() {
        // constructor rỗng → bắt buộc cho JPA
    }

    //Hibernate tự set (@CreationTimestamp, @UpdateTimestamp), không cần add vào controller

    public Order(User user, OrderStatusCode status) {
        this.user = user;
        this.status = status;
    }


    // ================== BUSINESS LOGIC ==================
    // Method này dùng để tính tổng tiền từ danh sách OrderItem
    // Không nên set totalPrice thủ công từ bên ngoài (tránh sai dữ liệu)

    public void calculateTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem orderItem : this.orderItems) {
            BigDecimal itemTotal = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            // total.add(itemTotal)-> nhớ gán total = ... Vì BigDecimal là immutable (bất biến), không thể gán trực tiếp, mà cần add(), -> tạo ra object total mới;
            total = total.add(itemTotal);
        }
        this.totalPrice = total;
    }

    public void addItem(OrderItem orderItem) {

        orderItems.add(orderItem);

        //Tthêm order_id vào OrderItems Table
        orderItem.setOrder(this);
        // Thiết lập quan hệ ngược lại:
        // gán Order hiện tại cho OrderItem
        // (this = Order hiện tại đang gọi method)

        //Giữ quan hệ 2 chiều, this = order hiện tại, setOrder(this) = gán item thuộc về order, order_id tự nhảy giá trị theo thứ tự
        //Order (id=1)
        //Item (order_id = 1)
        //Item (order_id = 1)

        // Nhờ dòng này:
        // - JPA sẽ biết item thuộc Order nào
        // - Khi save Order → OrderItem sẽ có order_id tương ứng

        calculateTotalPrice();
    }

    //item.setOrder(null) = “cắt quan hệ ở DB”
    //nếu không set null order_id vẫn còn -> dữ liệu sai lệch (inconsistent)
    public void removeItem(OrderItem orderItem) {
        orderItems.remove(orderItem);   // sync RAM
        orderItem.setOrder(null);       // sync owning side
        calculateTotalPrice();
    }
}