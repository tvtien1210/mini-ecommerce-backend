package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CartStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CurrencyCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.aspectj.weaver.ast.Or;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY: DB sẽ tự generate id (auto increment)
    // Lưu ý: id chỉ có sau khi entity được persist xuống DB
    private Long id;

    @ManyToOne(optional = false,fetch = FetchType.LAZY) //not null
    // LAZY: chỉ load user khi gọi getUser()
    // tránh load dư data (performance optimization)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    //Cart chỉ còn nhiệm vụ:chứa sản phẩm người dùng đang chọn,chờ checkout
    //bị xóa item sau khi thanh toán thành công nên không cần truy ngược(bỏ code này)
    //mappedBy = "cart" nghĩa là:
    //Cart không quản lý khóa ngoại, Order mới là bên giữ cart_id.
    //@OneToOne(mappedBy = "cart")
    //private Order order;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    // api remove = delete 🍺
    // mappedBy = "cart": CartItem là owner (giữ FK)
    // cascade = ALL: persist/update/delete Cart → tự động apply xuống CartItem
    // orphanRemoval = true:
    //  - nếu CartItem bị remove khỏi collection → sẽ bị delete trong DB
    //  - cực kỳ quan trọng để tránh "rác DB"
    @Builder.Default
    private Set<CartItem> cartItems = new HashSet<>();


    //Hai object này có đại diện cho cùng 1 row trong DB không?
    //Chỉ dùng id để so sánh
    //DB phân biệt bằng PRIMARY KEY -> Java cũng phải dùng id để phân biệt
    //id không thể thay đổi
    //Cart c1 = new Cart();
    //Cart c2 = new Cart();
    //id = null
    //nhưng không phải cùng object
    //Nếu không check:
    //2 object khác nhau sẽ bị coi là bằng nhau, phải check id != null, có giá trị
    //thì mới tồn tại 2 object khác nhau để so sánh equals

    @Override
    public boolean equals(Object o) {
        // 1. cùng object trong RAM
        if (this == o) return true;

        // 2. khác loại → không so
        // NOTE nâng cao:
        // Hibernate có thể dùng proxy (Cart$HibernateProxy)
        // instanceof vẫn OK, nhưng nếu dùng getClass() sẽ fail
        if (!(o instanceof Cart)) return false;

        // 3. ép kiểu
        Cart cart = (Cart) o;

        // 4. so id (identity trong DB)
        // Chỉ so khi id != null (đã persist)
        // Nếu id null → coi như object transient → không bằng nhau
        return id != null && id.equals(cart.id);
    }

    // KHÔNG dùng id trong hashCode vì:
    // Trước persist: id = null
    // Sau persist: id = 1
    // hashCode thay đổi → phá HashSet / HashMap
    //
    // Ví dụ:
    // Set<Cart> set = new HashSet<>();
    // Cart c = new Cart();
    // set.add(c); (hashCode A)
    // save(c) → id = 1 → hashCode B
    // set.contains(c) → FAIL (vì hashCode khác)
    //
    // getClass().hashCode() đảm bảo:
    // - stable (không đổi)
    // - vẫn phân biệt theo class
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    // lưu enum dạng string (ACTIVE, CHECKED_OUT...)
    // KHÔNG dùng ORDINAL vì dễ lỗi khi thay đổi thứ tự enum
    @Column(nullable = false)
    private CartStatusCode status = CartStatusCode.ACTIVE;

    @NotNull
    @PositiveOrZero
    @Setter(AccessLevel.NONE)
    // Setter NONE: không cho set từ ngoài → đảm bảo consistency
    // Chỉ update qua business logic (calculateTotalPrice)
    @Column(nullable = false, precision = 12, scale = 2)
    // precision=12, scale=2 → max 9999999999.99
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 3)
    private CurrencyCode currency = CurrencyCode.VND;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    // Hibernate tự set khi insert
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    // Hibernate tự update mỗi lần entity change
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Cart(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void calculateTotalPrice(){
        BigDecimal total = BigDecimal.ZERO;

        // loop qua tất cả CartItem
        for (CartItem cartItem : cartItems){

            // price * quantity
            // BigDecimal dùng để tránh sai số float/double (rất quan trọng trong tiền tệ)
            BigDecimal cartItemTotal =
                    cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            total = total.add(cartItemTotal);
        }

        // update lại tổng tiền
        this.totalPrice = total;
    }

    //Helper Methodđã
    public void addItem(CartItem cartItem){
        // add vào collection
        cartItems.add(cartItem);

        // set chiều ngược lại (IMPORTANT)
        // nếu không set → quan hệ 2 chiều bị lệch → bug rất khó debug
        cartItem.setCart(this);

        // luôn recalc sau khi thay đổi
        calculateTotalPrice();
    }

    public void removeItem(CartItem cartItem){
        cartItems.remove(cartItem);

        // set null để đảm bảo orphanRemoval hoạt động
        cartItem.setCart(null);

        // ⚠️ thiếu bước này là bug logic thường gặp
        // remove xong mà không recalc → totalPrice sai
        calculateTotalPrice();
    }
}