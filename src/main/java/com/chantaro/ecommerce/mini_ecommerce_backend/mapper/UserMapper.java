package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

// import DTO (dữ liệu trả ra API)🍺
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.CurrentUserDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.UserDTO;

// import Entity (dữ liệu từ DB)
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

// Class chuyên dùng để convert giữa Entity và DTO
public class UserMapper {
    // DTO = Data Transfer Object
    // Hàm static → gọi trực tiếp mà không cần tạo object UserMapper
    // Ví dụ: UserMapper.toDTO(user)
    public static UserDTO toDTO(User user) {

        Set<String> roles = user.getRoles()
                .stream()
                // = .map(role -> role.getName()), với mỗi Role, lấy name của nó
                // :: method reference.
                // (tham chiếu phương thức) là một cú pháp ngắn gọn của Lambda Expression
                // map(Role::getName) = lấy name ra khỏi từng Role, vì vậy Role → String
                .map(Role::getName)
                .collect(Collectors.toSet());


        // Tạo object UserDTO từ dữ liệu của User (Entity)
        return new UserDTO(

                user.getId(),        // lấy id từ DB
                user.getUsername(),     // lấy username
                user.getEmail(),        // lấy email
                user.getFullName(),     // lấy họ tên
                roles,                  // lấy role, yeu cau tra ve String -> vì map(Role::getName) = lấy name ra khỏi từng Role, vì vậy Role → String
                user.getCreatedAt()


                // không lấy password → tránh lộ thông tin nhạy cảm
                // không lấy roles → tránh vòng lặp JSON + giảm data trả về
        );
    }


    // Dùng riêng cho /api/auth/me
    public static CurrentUserDTO toDTO(
            User user,
            LocalDateTime expiresAt) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new CurrentUserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roles,
                user.getCreatedAt(),
                expiresAt
        );
    }
}