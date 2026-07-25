package com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


//@NoArgsConstructor
//@AllArgsConstructor
//@Getter
public class RegisterDTO {
    //id, username, email

    private Long id;
    private String username;
    private String email;

    public RegisterDTO() {
    }

    public RegisterDTO(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
