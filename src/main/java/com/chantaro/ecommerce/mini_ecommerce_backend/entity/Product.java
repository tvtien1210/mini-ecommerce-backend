package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CurrencyCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter // Lombok: tự động tạo getter cho tất cả field
@Setter // Lombok: tự động tạo setter cho tất cả field
@Entity // Đánh dấu class này là entity (map với bảng product trong DB)
@Table(name = "product", indexes = {
        // Tạo index cho category_id → giúp query theo category nhanh hơn
        @Index(name = "idx_category_id", columnList = "category_id")
})
public class Product {

    @Id // Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // ID tự tăng (auto increment trong DB)
    private Long id;

    @NotBlank
    // Validate: không được null, không rỗng, không toàn khoảng trắng
    @Column(nullable = false, length = 255)
    // nullable = false → DB không cho null
    // length = 255 → giới hạn độ dài tên sản phẩm
    private String name;

    @Column(columnDefinition = "TEXT")
    // Dùng TEXT để lưu mô tả dài (không bị giới hạn như VARCHAR)
    private String description;

    @NotNull
    // Không được null
    @Positive
    // Giá phải > 0
    @Column(nullable = false, precision = 10, scale = 2)
    // DECIMAL(10,2): tối đa 10 chữ số, 2 số sau dấu phẩy
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 3, nullable = false)
    private CurrencyCode currency;

    @NotNull
    @Min(0)
    // Số lượng tồn kho phải ≥ 0
    @Column(nullable = false)
    private Integer stock = 0;
    // mặc định = 0 (khi chưa set)

    //reserved_stock INT NOT NULL DEFAULT 0
    @NotNull
    @Min(0)
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    // Lock stock (race condition),
    // khoá số lượng tồn kho sau khi trừ, sau khi có user load mua hàng trước, ai nhanh người ấy được
    @Version
    private Long version;

    //Cho phep null, nullable = true
    //Khong cho phep null, nullable = false
    @Column(name = "image_url", nullable = false,length = 1000)
    //@NotBlank
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // Nhiều Product thuộc về 1 Category
    // LAZY: chỉ load category khi cần → tối ưu performance
    // optional = false: bắt buộc phải có category (không được null)
    @JoinColumn(name = "category_id", nullable = false)
    // map với cột category_id trong DB (NOT NULL)
    private Category category;

    @CreationTimestamp
    // Hibernate tự set thời gian khi insert (tạo mới)
    @Column(name = "created_at", updatable = false)
    // updatable = false: không cho update field này, vd nếu có update name product, thì cũng không tự động update createdAt
    // tránh bị ghi đè khi update entity
    private LocalDateTime createdAt;

    @UpdateTimestamp
    // Hibernate tự cập nhật thời gian mỗi khi update entity
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor rỗng (JPA bắt buộc phải có)
    public Product() {
    }

    // Constructor tiện để tạo object nhanh
    public Product(String name, String description, BigDecimal price, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }


}