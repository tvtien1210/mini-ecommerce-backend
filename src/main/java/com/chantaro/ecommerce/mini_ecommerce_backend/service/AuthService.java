package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.CreateRegisterRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.RegisterDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.RoleRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public RegisterDTO register(CreateRegisterRequest request) {

        //check username ton tai chua? neu chua nem exception
        //errorCode tham so = ErrorCode.USERNAME_ALREADY_EXISTS la doi so
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS); //neu mail da ton tai, code se dung tai day nho throw new ex
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        // Neu mail chua toi tai tao moi user
        // Create User
        User user = new User();

        user.setUsername(request.getUsername());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setEmail(request.getEmail());

        user.setFullName(request.getFullName());

        //ROLE
        Role role = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(()-> new RuntimeException("ROLE_CUSTOMER not found"));

        user.addRole(role);

        User savedUser = userRepository.save(user);

        return new RegisterDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()

        );


    }
}


/*

Them user.addRole(role);
thì flow đăng ký sẽ trở thành:
Register
    ↓
Tạo User
    ↓
BCrypt encode password
    ↓
Lấy ROLE_CUSTOMER từ database
    ↓
user.addRole(role)
    ↓
userRepository.save(user)
    ↓
users_roles được tạo

Sau đó, khi login:
Login
    ↓
CustomUserDetailsService.loadUserByUsername()
    ↓
Lấy User + Roles
    ↓
Spring Security xác thực password
    ↓
JWT được tạo*/