package com.chantaro.ecommerce.mini_ecommerce_backend.security.config;

// ===== Import JWT =====

import com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt.JwtFilter;

// ===== Import Spring =====
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ===== Import Spring Security =====
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Đánh dấu đây là class cấu hình của Spring
// Spring sẽ đọc class này để tạo bean (giống file config)
@Configuration

// Bật Spring Security cho ứng dụng
// Kích hoạt toàn bộ cơ chế bảo mật: authentication + authorization
@EnableWebSecurity

//Bật chức năng dùng @PreAuthorize (trong service) trong toàn bộ project
@EnableMethodSecurity

// Tự động tạo constructor chứa TẤT CẢ field "final" trong class
// Dùng để inject dependency nhanh (constructor injection)
// Tránh phải viết constructor thủ công
public class SecurityConfig {

    // ===== JWT FILTER =====
    // Filter dùng để check token mỗi request
    // final = gán 1 lần duy nhất + đảm bảo không bị thay đổi reference (tham chieu)
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // ===== PASSWORD ENCODER =====
    // Bean mã hoá password bằng BCrypt
    // -> dùng khi lưu user và khi login
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===== DAO AUTHENTICATION PROVIDER =====
    // Đây là tạo class xử lý login bằng DB
    // -> dùng UserDetailsService để load user
    // -> dùng PasswordEncoder để check password
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService) {

        // Tạo provider
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Gán service lấy user từ DB (username + password), Khi login → gọi loadUserByUsername()
        authProvider.setUserDetailsService(userDetailsService);

        // Gán encoder để so sánh password
        // match() rawPassword (user nhập) vs encodedPassword (DB)
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // ===== AUTHENTICATION MANAGER =====
    // Đây là "bộ não login"
    // -> sẽ sử dụng DaoAuthenticationProvider ở trên

    // AuthController (login API)
    // Gọi authenticate(...)
    // => NÊN giữ lại đoạn này
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {

        // Spring sẽ build AuthenticationManager dựa trên:
        // - DaoAuthenticationProvider
        // - UserDetailsService
        // - PasswordEncoder

        return config.getAuthenticationManager();
    }

    // ===== SECURITY FILTER CHAIN =====
    // Cấu hình toàn bộ bảo mật hệ thống
    @Bean
    //DaoAuthenticationProvider phải là bean độc lập
    //Không nên inject trực tiếp vào filterChain
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ===== TẮT CSRF =====
                // Vì đây là REST API (không dùng form)
                .csrf(csrf -> csrf.disable())


                // ===== PHÂN QUYỀN =====
                .authorizeHttpRequests(auth -> auth

                        // =========================================================
                        // PUBLIC PAGES
                        // =========================================================

                        .requestMatchers(
                                "/",
                                "/login",
                                "/register/**"
                        ).permitAll()


                        // =========================================================
                        // PUBLIC API - AUTHENTICATION
                        // =========================================================

                        // Login / Register / Authentication
                        .requestMatchers("/api/auth/**")
                        .permitAll()


                        // =========================================================
                        // PUBLIC RESOURCES
                        // =========================================================

                        // Static resources
                        .requestMatchers(
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()


                        // =========================================================
                        // USER API
                        // =========================================================

                        // Current authenticated user
                        // GET /api/users/me
                        .requestMatchers("/api/users/me")
                        .authenticated()


                        // User management API
                        // CUSTOMER / STAFF / ADMIN
                        .requestMatchers("/api/users/**")
                        .hasAnyRole(
                                "CUSTOMER",
                                "STAFF",
                                "ADMIN"
                        )


                        // =========================================================
                        // PRODUCT API
                        // =========================================================

                        // View products
                        // CUSTOMER / STAFF / ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "STAFF",
                                "ADMIN"
                        )


                        // Create product
                        // ADMIN only
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products/**"
                        ).hasRole("ADMIN")


                        // Update product
                        // ADMIN only
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/**"
                        ).hasRole("ADMIN")


                        // Delete product
                        // ADMIN only
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/**"
                        ).hasRole("ADMIN")


                        // =========================================================
                        // CUSTOMER - CART
                        // =========================================================

                        // Customer cart
                        // Add / Update / Remove / View cart
                        .requestMatchers("/api/cart/**")
                        .hasRole("CUSTOMER")


                        // =========================================================
                        // CUSTOMER - ORDER
                        // =========================================================

                        // My Orders page
                        // Customer only
                        .requestMatchers("/myorders")
                        .permitAll()


                        // Checkout
                        // Cart -> Create Order
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders/checkout"
                        ).hasRole("CUSTOMER")


                        // Get current customer's orders
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/my"
                        ).hasRole("CUSTOMER")


                        // =========================================================
                        // STAFF / ADMIN - ORDER MANAGEMENT
                        // =========================================================

                        // Update order status
                        // STAFF / ADMIN
                        // PUT /api/orders/{id}/status
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/orders/*/status"
                        ).hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )


                        // View all orders
                        // STAFF / ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/**"
                        ).hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )


                        // =========================================================
                        // STAFF PAGES
                        // =========================================================

                        .requestMatchers("/staff/**")
                        .hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )


                        // =========================================================
                        // ADMIN PAGES
                        // =========================================================

                        // Admin Dashboard
                        .requestMatchers("/admin")
                        .permitAll()


                        // Admin Management Pages
                        // /admin/products
                        // /admin/orders
                        // /admin/users
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")


                        // =========================================================
                        // VNPAY
                        // =========================================================

                        // VNPay return / IPN callback
                        // VNPay server must be able to access these endpoints
                        .requestMatchers(
                                "/api/payment/return",
                                "/api/payment/ipn"
                        ).permitAll()


                        // =========================================================
                        // EVERYTHING ELSE
                        // =========================================================

                        .requestMatchers("/403").permitAll()

                        // All remaining endpoints require authentication
                        .anyRequest()
                        .authenticated()
                )

                // ===== JWT FILTER =====
                // Request đi qua JWT trước
                // Nếu token hợp lệ:
                // SecurityContext sẽ có Authentication


                // ===== ĐĂNG KÝ PROVIDER =====
                // dùng DaoAuthenticationProvider của mình
                // khi login sẽ:
                //    -gọi UserDetailsService
                //    -lấy user từ DB
                //    -check password bằng BCrypt


                // ===== JWT FILTER =====
                // Thêm filter trước UsernamePasswordAuthenticationFilter
                // → nghĩa là:
                // request sẽ đi qua JWT trước
                // nếu có token hợp lệ → cho qua luôn
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


/*CUSTOMER
 |
 | POST /api/cart/items
 | POST /api/orders/checkout
 ↓
ORDER


STAFF
 |
 | GET /api/orders
 | PUT /api/orders/{id}/status
 ↓
PROCESS ORDER


ADMIN
 |
 | CRUD Product
 | Manage User
 | View/Update Order*/