package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.CreateRegisterRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.RegisterDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.CurrentUserDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.UserMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.RoleRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;


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
                .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));

        user.addRole(role);

        User savedUser = userRepository.save(user);

        return new RegisterDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()

        );


    }

    public CurrentUserDTO getCurrentUser(HttpServletRequest request) {

        // Lấy Authentication mà JwtFilter đã tạo
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra user đã đăng nhập chưa
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy username từ Authentication
        String username = authentication.getName();

        // Tìm user trong database
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        // Lấy Cookie accessToken từ request
        Cookie cookie = WebUtils.getCookie(request, "accessToken");

        // Biến lưu thời gian JWT hết hạn
        LocalDateTime expiresAt = null;

        if (cookie != null) {

            // Lấy thời gian hết hạn từ JWT
            //extractExpiration(cookie.getValue()); tra ve kieu du lieu Date
            Date expiration = jwtService.extractExpiration(cookie.getValue());
            //Convert expiration vua lay duoc tu token trong cookie sang LocalDateTime
            expiresAt = expiration.toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime();
        }

        System.out.println("Check expires at ------" + expiresAt);

        return UserMapper.toCurrentUserDTO(user, expiresAt);
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