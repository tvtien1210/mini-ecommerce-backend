package com.chantaro.ecommerce.mini_ecommerce_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


//Nếu không để ý: User tự set mình thành role ADMIN 💀 khi dùng User Entity -> nên cần có DTO CreateUserRequest này
@Getter
@NoArgsConstructor
@AllArgsConstructor
    public class CreateUserRequest {
        private String username;
        private String password;
        private String email;
        private String fullName;
    }
