package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // giống Cart: id chỉ có sau khi persist
    // trước đó entity ở trạng thái transient
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // LAZY: chỉ load Cart khi cần (getCart)
    // tránh join dư → giảm query cost
    @JoinColumn(name = "cart_id", nullable = false)
    // FK NOT NULL → CartItem bắt buộc phải thuộc về 1 Cart
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // Product cũng LAZY → tránh load toàn bộ product khi load cart
    @JoinColumn(name = "product_id", nullable = false)
    // FK NOT NULL → CartItem luôn phải có product
    private Product product;

    @Override
    // 	o = object bất kỳ, có thể là:Cart, String, Integer, hoặc bất cứ gì
    public boolean equals(Object o) {

        if (this == o) return true; // cùng reference: cùng địa chỉ RAM (==)

        if (!(o instanceof CartItem)) return false; // cùng type : class (Cart, String…)

        // NOTE nâng cao:
        // Hibernate dùng proxy (CartItem$HibernateProxy)
        // instanceof vẫn hoạt động OK (khác với getClass())

        CartItem cartItem = (CartItem) o; // ép kiểu

        // so id
        // chỉ so khi id != null (đã persist)
        // nếu chưa persist → luôn false (tránh bug logic)
        return id != null && id.equals(cartItem.id);
    }

    @Override
    //hashCode = số để Java phân loại object
    public int hashCode() {

        // KHÔNG dùng id vì:
        // id thay đổi sau persist → phá HashSet/HashMap
        //
        // dùng getClass().hashCode():
        // - ổn định (stable)
        // - không phụ thuộc trạng thái entity
        return getClass().hashCode();
    }

    @NotNull
    @Positive
    @Column(nullable = false)
    // quantity > 0 (business rule)
    // default = 1 → khi add item mới
    private Integer quantity = 1;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    // giá tại thời điểm add vào cart (snapshot price)
    // KHÔNG nên lấy trực tiếp từ Product vì:
    // - giá product có thể thay đổi
    // - cần giữ lịch sử giá lúc user add vào cart
    private BigDecimal price;

    public CartItem(Integer quantity, BigDecimal price) {
        this.quantity = quantity;
        this.price = price;
    }

    public void increaseQuantity(int amount){

        // validate input (kiểm tra dữ liệu đầu vào)
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be > 0");

        this.quantity += amount;

        cart.calculateTotalPrice();

        // ⚠️ NOTE QUAN TRỌNG:
        // KHÔNG tự gọi cart.calculateTotalPrice()
        // vì CartItem không phải aggregate root
        // → để Cart chịu trách nhiệm consistency toàn bộ

        // aggregate root (thực thể gốc quản lý logic)
        // consistency (tính nhất quán dữ liệu)
        // helper method (hàm hỗ trợ)
        // inconsistency (lệch dữ liệu)
    }

    public void decreaseQuantity(int amount) {

        // validate input: không cho giảm số lượng <= 0
        // tránh case truyền nhầm số âm hoặc 0
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be > 0");

        // tính toán số lượng mới sau khi giảm
        int newQuantity = this.quantity - amount;

        // business rule:
        // quantity luôn phải > 0
        // nếu = 0 hoặc âm → không hợp lệ
        //
        // NOTE:
        // - không cho quantity = 0
        // - vì nếu = 0 thì nên remove CartItem khỏi Cart
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be > 0");
        }

        // cập nhật quantity
        this.quantity = newQuantity;

        cart.calculateTotalPrice();

        // ⚠️ IMPORTANT:
        // không gọi cart.calculateTotalPrice() ở đây
        // vì CartItem không phải aggregate root
        // → Cart sẽ chịu trách nhiệm đảm bảo consistency toàn bộ
    }

    // Không cần helper vì
    // Cart (1) --- (n) CartItem
    // Cart là owner logic (aggregate root)
    // Chỉ thao tác qua “root” (Cart)
    //
    // NOTE thêm:
    // Không nên gọi cartItem.setCart(...) từ ngoài
    // → luôn dùng Cart.addItem() để đảm bảo:
    //   - set 2 chiều
    //   - cập nhật totalPrice
    //   - tránh inconsistency

}