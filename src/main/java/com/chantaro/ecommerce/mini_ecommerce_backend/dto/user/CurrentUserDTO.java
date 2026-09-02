package com.chantaro.ecommerce.mini_ecommerce_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Getter
// Thêm CurrentUserDTO riêng,
// để không ảnh hưởng tới UserDTO gốc đang gọi ở các api liên quan khác không có expiresAt
public class CurrentUserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}