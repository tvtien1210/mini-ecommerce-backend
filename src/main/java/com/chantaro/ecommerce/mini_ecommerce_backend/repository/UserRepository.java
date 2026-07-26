package com.chantaro.ecommerce.mini_ecommerce_backend.repository;

// Import Entity User

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;

// Import thư viện JPA
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// Đánh dấu đây là Repository (tầng truy cập DB)
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// JpaRepository<Entity, Kiểu dữ liệu của ID>
// Ở đây: User là entity, Long là kiểu của id

public interface UserRepository extends JpaRepository<User, Long> {

    // Cách 1: Giữ nguyên @Query của bạn (đã bỏ @Param cho gọn)
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    // Cách 2: Dùng @EntityGraph (Thay thế cho JOIN FETCH, code ngắn gọn hơn)
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByEmail(String email);

    // SỬA TẠI ĐÂY: Không dùng @Query, không dùng JOIN FETCH cho hàm exists
    boolean existsByEmail(String email);
}



/*
        @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
        Optional<User> findByUsername(@Param("username") String username);

        Ý nghĩa
        User u → entity User
        JOIN FETCH u.roles → load luôn roles
        :username → tham số truyền vào

        Lấy User + roles trong 1 query
        So sánh
        JOIN ❌ → roles vẫn LAZY (chưa load)
        JOIN FETCH ✅ → roles load ngay

        Mục đích
        Tránh LazyInitializationException
        Dùng khi:
        Login (Spring Security)
        JWT filter
        API cần roles

        Best practice
        SELECT DISTINCT u FROM User u JOIN FETCH u.roles WHERE u.username = :username
        Tránh duplicate khi join nhiều bảng

        JOIN FETCH = load luôn dữ liệu liên quan (tránh lazy lỗi)
*/

