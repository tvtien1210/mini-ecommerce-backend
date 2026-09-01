package com.chantaro.ecommerce.mini_ecommerce_backend.dto.user;

// Lombok: tự động tạo getter cho tất cả field

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

// DTO dùng để trả dữ liệu ra API (KHÔNG phải dữ liệu DB)
@Getter // tự tạo getId(), getUsername(), ...
@AllArgsConstructor // tự tạo constructor đầy đủ tham số
public class UserDTO {

    private Long id;                    // id của user

    private String username;            // tên đăng nhập

    private String email;               // email

    private String fullName;            // họ và tên

    private Set<String> roles;          // phân quyền, tai sao la Set<String> -> xem UserMapper

    private LocalDateTime createdAt;    // thời gian tạo tài khoản

    // không có password → tránh lộ thông tin nhạy cảm
}