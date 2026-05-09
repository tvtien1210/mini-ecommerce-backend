package com.chantaro.ecommerce.mini_ecommerce_backend.dto.update;

import lombok.AllArgsConstructor;
import lombok.Getter;


//Nếu không để ý: User tự update thành role ADMIN 💀 khi dùng User Entity -> nên cần có DTO UpdateUserRequest này
@Getter
@AllArgsConstructor
    public class UpdateUserRequest {
        private String username;
        private String password;
        private String email;
        private String fullName;
    }
