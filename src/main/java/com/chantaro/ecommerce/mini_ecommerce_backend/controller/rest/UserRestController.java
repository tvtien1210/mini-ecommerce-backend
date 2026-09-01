package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

// import DTO (dữ liệu trả ra API)

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.CreateUserRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.update.UpdateUserRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.UserDTO;

// import Service (chứa business logic)
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.UserMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Đánh dấu đây là REST Controller → dùng để tạo API trả về JSON
@RestController

// Định nghĩa URL gốc cho tất cả API trong class này
// => API đầy đủ sẽ là: /api/users
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRestController {

    // Service để xử lý logic (không làm việc trực tiếp với DB)
    private final UserService userService;
    private final UserRepository userRepository;


    // API GET /api/users
    // dùng để lấy danh sách tất cả user
    @GetMapping
    public List<UserDTO> getAll() {

        // gọi xuống Service để lấy dữ liệu
        return userService.getAllUsers();

        // Service sẽ:
        // - lấy data từ DB (Repository)
        // - convert Entity → DTO (Mapper)
        // - trả về List<UserDTO>
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDTO createUser(@RequestBody CreateUserRequest req) {
        return UserMapper.toDTO(userService.saveUser(req));
    }

    @PutMapping("/{id}")
    public UserDTO update(@PathVariable Long id, @RequestBody UpdateUserRequest rq) {
        return UserMapper.toDTO(userService.updateUser(id, rq));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}

/*
Controller ↔ DTO
        ↓
     Service ↔ Entity
        ↓
   Repository ↔ Entity
*/