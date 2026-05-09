package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

// import DTO (dữ liệu trả ra API)🍺
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.UserDTO;

// import Entity (dữ liệu từ DB)
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;

// Class chuyên dùng để convert giữa Entity và DTO
public class UserMapper {
    // DTO = Data Transfer Object
    // Hàm static → gọi trực tiếp mà không cần tạo object UserMapper
    // Ví dụ: UserMapper.toDTO(user)
    public static UserDTO toDTO(User user) {

        // Tạo object UserDTO từ dữ liệu của User (Entity)
        return new UserDTO(

                user.getId(),        // lấy id từ DB
                user.getUsername(),  // lấy username
                user.getEmail(),     // lấy email
                user.getFullName(),  // lấy họ tên
                user.getCreatedAt()  // lấy thời gian tạo

                // không lấy password → tránh lộ thông tin nhạy cảm
                // không lấy roles → tránh vòng lặp JSON + giảm data trả về
        );
    }
}