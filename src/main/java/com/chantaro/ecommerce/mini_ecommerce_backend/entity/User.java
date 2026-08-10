package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

// ===== JPA (mapping DB) =====

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

// ===== Validation (check dữ liệu đầu vào) =====
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
// Lombok: tự động sinh getter/setter

@Entity
// Đánh dấu class này là Entity → map với bảng trong DB

@Table(name = "users")
// map class User với bảng "users"
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // PRIMARY KEY, auto increment
    private Long id;


    @NotBlank
    // không được null hoặc rỗng

    @Column(length = 50, nullable = false, unique = true)
    // DB:
    // - NOT NULL
    // - UNIQUE
    // - max 50 ký tự
    private String username;


    @NotBlank
    // không trả password ra API (bảo mật)

    @Column(length = 255, nullable = false)
    // lưu password đã mã hoá (bcrypt)
    private String password;


    @Email
    // validate format email

    @Column(length = 100, unique = true)
    // email có thể null nhưng nếu có thì phải unique
    private String email;


    @NotBlank
    @Column(name = "full_name", length = 100)
    // map field fullName → cột full_name trong DB
    private String fullName;


    @Column(updatable = false)
    // không cho update sau khi insert

    @CreationTimestamp
    // Hibernate tự set thời gian hiện tại khi tạo
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    // 1 User có nhiều Order
    // mappedBy = "user" → bên Order giữ FK (user_id)
    // LAZY: chỉ load khi gọi getOrders(), đỡ gây tốn tài nguyên, tối ưu performancez
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Cart> carts = new HashSet<>(); //tránh null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_roles",
            // bảng trung gian

            joinColumns = @JoinColumn(name = "user_id"),
            // FK trỏ tới User

            inverseJoinColumns = @JoinColumn(name = "role_id")
            // FK trỏ tới Role
    )
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();
    // danh sách role của user
    // dùng Set để tránh trùng
    // init new HashSet<>() sẵn để tránh NullPointerException


    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        // cùng object → bằng nhau

        if (!(o instanceof User)) return false;
        // khác loại → không so sánh

        User user = (User) o;

        // so sánh theo id (duy nhất)
        return id != null && id.equals(user.id);
    }


    @Override
    public int hashCode() {
        // giữ ổn định khi id = null
        return getClass().hashCode();
    }


    public User() {
    }
    // constructor rỗng (bắt buộc JPA)


    public User(String username, String password, String email, String fullName) {
        // tạo nhanh object user
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }


    //User này có Role này, và Role này cũng có User này.
    public void addRole(Role role) {
        // Thêm role vào danh sách roles của User hiện tại
        this.roles.add(role);
        // lấy danh sách User của Role, thêm chính User hiện tại vào danh sách đó (giữ quan hệ 2 chiều)
        role.getUsers().add(this);
        // đảm bảo:  User <-> Role luôn đồng bộ
    }


    public void removeRole(Role role) {

        this.roles.remove(role);
        // xóa role khỏi user

        role.getUsers().remove(this);
        // xóa user khỏi role

        // 👉 tránh lệch dữ liệu 2 chiều
    }
}
