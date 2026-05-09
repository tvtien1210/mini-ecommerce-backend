package com.chantaro.ecommerce.mini_ecommerce_backend.entity;

// ===== JPA (mapping DB) =====
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

// ===== Validation =====
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
// Lombok: tự động sinh getter/setter cho tất cả field

@Entity
// Đánh dấu class này là Entity → map với bảng trong database

@Table(name = "roles")
// map class Role với bảng "roles" trong DB
public class Role {

    @Id
    // Khóa chính (PRIMARY KEY)

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // AUTO_INCREMENT (DB tự tăng id)
    private Integer id;


    @NotBlank
    // Validation:
    // - không được null
    // - không được rỗng ("")

    @Column(length = 50, nullable = false, unique = true)
    // DB constraint:
    // - NOT NULL
    // - UNIQUE (không trùng)
    // - max 50 ký tự
    private String name;


    @Column(length = 255)
    // mô tả role (có thể null)
    private String description;


    @ToString.Exclude
    // Không include field này khi gọi toString()
    // Tránh vòng lặp vô hạn:
    // Role -> User -> Role -> User -> ...

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    // Quan hệ MANY-TO-MANY với User
    //
    // mappedBy = "roles":
    // → bên User là OWNER (quản lý bảng trung gian users_roles)
    // → Role là phía bị động (inverse side)
    //
    // fetch = LAZY:
    // → KHÔNG load users ngay khi load Role
    // → chỉ load khi gọi role.getUsers()
    // → giúp tối ưu performance (tránh query dư thừa)

    @JsonIgnore
    // Khi trả JSON (API), bỏ field này đi
    // Tránh:
    // - vòng lặp vô hạn JSON
    // - response quá lớn

    private Set<User> users = new HashSet<>();
    // role.getUsers().add(user); // ❌ vẫn gọi được từ ngoài
    // HashSet<> fix "vẫn gọi được từ ngoài" -> trở thành không gọi được từ ngoài
    //	Set<User> users = new HashSet<>() -> users là object thật ,role.getUsers().add(user) chạy bình thường, không bị null
    // Tránh NullPointerException : NPE xảy ra khi bạn dùng một object đang = null
    // Dùng Set để:
    // - tránh duplicate user
    // - đúng bản chất quan hệ ManyToMany
    //
    // Khởi tạo sẵn:
    // → tránh NullPointerException khi gọi add/remove


    @Override
    public boolean equals(Object o) {

        // Nếu cùng reference (cùng vùng nhớ) → chắc chắn bằng nhau
        if (this == o) return true;

        // Nếu object không phải Role → không so sánh
        if (!(o instanceof Role)) return false;

        // Ép kiểu về Role
        Role role = (Role) o;

        // So sánh theo id (duy nhất trong DB)
        // Nếu id khác null và bằng nhau → coi là cùng entity
        return id != null && id.equals(role.id);
    }


    @Override
    public int hashCode() {

        // Trả về hashCode của class
        // Không dùng id trực tiếp vì:
        // → khi entity chưa persist thì id = null
        //
        // Đây là best practice trong JPA
        return getClass().hashCode();
    }


    public Role() {}
    // Constructor rỗng (BẮT BUỘC cho JPA)


    public Role(String name, String description) {
        // Constructor tạo nhanh object Role
        // Không cho set users → tránh phá quan hệ ManyToMany
        this.name = name;
        this.description = description;
    }


    public void addUser(User user){

        this.users.add(user);
        // Thêm user vào danh sách users của role

        user.getRoles().add(this);
        // Thiết lập quan hệ ngược:
        // thêm role hiện tại vào danh sách roles của user

        // Giữ quan hệ 2 chiều luôn đồng bộ:
        // Role <-> User

        // Nếu thiếu dòng này:
        // - DB có thể vẫn lưu
        // - nhưng object trong memory bị lệch (bug khó debug)
    }


    public void removeUser(User user){

        this.users.remove(user);
        // Xóa user khỏi danh sách của role

        user.getRoles().remove(this);
        // Xóa quan hệ ngược:
        // user không còn chứa role này nữa

        // Giữ data đồng bộ 2 chiều
        // Tránh:
        // - dữ liệu "lệch"
        // - lỗi logic sau này
    }
}
