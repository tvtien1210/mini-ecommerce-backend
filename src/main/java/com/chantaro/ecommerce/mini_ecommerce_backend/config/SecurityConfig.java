package com.chantaro.ecommerce.mini_ecommerce_backend.config;

// ===== Import JWT =====
import com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt.JwtFilter;


// ===== Import Spring =====
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ===== Import Spring Security =====
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

                        // ===== PUBLIC =====
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/register/**").permitAll()

                        // API login/register → không cần token
                        .requestMatchers("/api/auth/**").permitAll()

                        // ===== USER API =====
                        // phải có role mới vào được
                        .requestMatchers("/api/users/**")
                        .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                        // ===== PRODUCT API =====
                        // phải có role mới vào được
                        .requestMatchers("/api/products/**")
                        .hasAnyRole("CUSTOMER", "STAFF", "ADMIN")

                        // ===== ADMIN =====
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ===== STAFF =====
                        .requestMatchers("/staff/**")
                        .hasAnyRole("STAFF", "ADMIN")

                        // ===== ORDER =====
                        .requestMatchers("/api/orders/my").hasRole("CUSTOMER")
                        .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/api/orders/{id}/status").hasAnyRole("ADMIN","STAFF")

                        // public resources
                        .requestMatchers(
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico"
                        ).permitAll()

                        // ===== CÒN LẠI =====
                        .anyRequest().authenticated()
                )

                // ===== ĐĂNG KÝ PROVIDER =====
                // → dùng DaoAuthenticationProvider của mình
                // → khi login sẽ:
                //    1. gọi UserDetailsService
                //    2. lấy user từ DB
                //    3. check password bằng BCrypt
                .authenticationProvider(daoAuthenticationProvider(null)) //Spring inject tự động

                // ===== JWT FILTER =====
                // Thêm filter trước UsernamePasswordAuthenticationFilter
                // → nghĩa là:
                // request sẽ đi qua JWT trước
                // nếu có token hợp lệ → cho qua luôn
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}