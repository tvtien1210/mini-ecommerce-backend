package com.chantaro.ecommerce.mini_ecommerce_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@Getter
public class CurrentUserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private String expiresAt;
}