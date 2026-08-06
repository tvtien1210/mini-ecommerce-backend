package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter // Lombok: tự động tạo getter cho tất cả field
@Setter // Lombok: tự động tạo setter cho tất cả field
@Entity // Đánh dấu class này là 1 entity (map với table trong DB)
@Table(name = "order_item", indexes = {
        // Tạo index để query nhanh hơn theo order_id
        @Index(name = "idx_order_id", columnList = "order_id"),
        // Tạo index để query nhanh hơn theo product_id
        @Index(name = "idx_product_id", columnList = "product_id")
})
public class OrderItem {

    @Id // Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto increment (DB tự tăng id)
    private Long id;

    //Mục đích là tham chiếu đến đúng orderItem đã check out, để xoá cartItem trong giỏ hàng tương ứng một cách
    //chính xác, không bị xoá nhầm sản phẩm khách vừa mới thêm ở tab khác trong khi đanh vnpay, tránh race condition(tình huống tương tranh gây ra kết quả sai lệch)
    private Long cartItemId;

    // Tránh vòng lặp vô hạn (Order -> OrderItem -> Order ...)
    // @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    // Nhiều OrderItem thuộc về 1 Order
    // LAZY: chỉ load khi cần (tối ưu performance)
    // optional = false: bắt buộc phải có Order (không được null)
    // dòng này cho field mapto, la field Order, load OrderItem trước, load Order sau
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    // map với cột order_id trong DB (NOT NULL)
    private Order order;

    //@ToString.Exclude
    //@EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // Nhiều OrderItem thuộc về 1 Product
    @JoinColumn(name = "product_id", nullable = false)
    // map với cột product_id (NOT NULL)
    private Product product;

    @NotNull
    @Column(nullable = false)
    private String productName;


    @NotNull // validate: không được null
    @Positive // validate: phải > 0
    @Column(nullable = false)
    // map với cột quantity (NOT NULL)
    private Integer quantity;

    // snapshot giá tại thời điểm mua (không bị ảnh hưởng khi Product đổi giá)
    @NotNull // không được null
    @PositiveOrZero // >= 0 (có thể 0 nếu free item, discount)
    @Column(nullable = false, precision = 10, scale = 2)
    // DECIMAL(10,2): tối đa 10 chữ số, 2 số sau dấu phẩy
    private BigDecimal price;



    // constructor rỗng (JPA bắt buộc phải có)
    public OrderItem() {
    }

    // constructor tiện dùng khi tạo object

    public OrderItem(Long id, String productName, Integer quantity, BigDecimal price) {
        this.id = id;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }
}