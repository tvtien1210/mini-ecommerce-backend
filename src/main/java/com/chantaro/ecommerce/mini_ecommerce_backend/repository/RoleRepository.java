package com.chantaro.ecommerce.mini_ecommerce_backend.repository;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//	Dùng interface để Spring Data JPA tự tạo implementation tự động → tiết kiệm code, dễ maintain.
//	Nếu dùng class, phải tự viết logic thao tác database, mất đi ưu điểm chính của Spring Data.
@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
    //// Trả về Optional để xử lý trường hợp user không tồn tại null an toàn hơn, xu ly ngoai le orElseThrow
    Optional<Role> findByName(String name);
}
