package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

// Import JPA để mapping DB
import jakarta.persistence.*;

// Validate: không cho phép null hoặc chuỗi rỗng
import jakarta.validation.constraints.NotBlank;

// Lombok: tự tạo getter/setter
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

// Hibernate: tự động set thời gian tạo
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

// Tạo getter cho tất cả field
@Getter
// Tạo setter cho tất cả field (trừ những field bị chặn bằng AccessLevel.NONE)
@Setter
@Entity // Đánh dấu đây là entity (map với bảng DB)
@Table(name = "category") // Tên bảng trong DB
public class Category {

    // ================== PRIMARY KEY ==================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto increment
    @Setter(AccessLevel.NONE) // không cho set id thủ công
    private Integer id;

    // ================== NAME ==================
    @NotBlank // không được null hoặc rỗng
    @Column(nullable = false, length = 100) // NOT NULL + max 100 ký tự
    private String name;

    // ================== DESCRIPTION ==================
    @Column(columnDefinition = "TEXT") // kiểu TEXT trong DB
    private String description;

    // ================== RELATIONSHIP ==================
    // 1 Category có nhiều Product
    // mappedBy = "category" → field category nằm bên Product (owner là Product)
    // LAZY → chỉ load products khi gọi getProducts()
    @Setter(AccessLevel.NONE) // không cho set cả list từ ngoài (tránh phá quan hệ)
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products;

    // ================== EQUALS ==================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // cùng object → true
        if (!(o instanceof Category)) return false; // khác kiểu → false

        Category category = (Category) o;

        // chỉ so sánh khi id != null (đã lưu DB)
        return id != null && id.equals(category.id);
    }

    // ================== HASHCODE ==================
    @Override
    public int hashCode() {
        // tất cả object cùng class → cùng hash
        // (an toàn cho Hibernate, tránh bug khi id chưa có)
        return getClass().hashCode();
    }

    // ================== CREATED TIME ==================
    @Setter(AccessLevel.NONE) // không cho set tay
    @CreationTimestamp // Hibernate tự set khi insert
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ================== CONSTRUCTOR ==================
    public Category() {
        // constructor rỗng → bắt buộc cho JPA
    }

    // constructor dùng khi tạo mới Category
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }


    // ================== HELPER METHOD ==================
    public void addProduct(Product product){
        // thêm product vào list
        products.add(product);

        // set ngược lại category cho product
        // → giữ quan hệ 2 chiều luôn đồng bộ
        product.setCategory(this);
    }
}
