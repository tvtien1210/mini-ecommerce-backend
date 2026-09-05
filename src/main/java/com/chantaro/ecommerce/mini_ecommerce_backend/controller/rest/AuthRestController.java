package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.login.LoginRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.register.CreateRegisterRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.CurrentUserDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.security.service.CustomUserDetailsService;
import com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt.JwtService;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthRestController {
    // Dung de check login
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthService authService;
    private final UserRepository userRepository;


    // POST REGISTER, dang ky nguoi dung moi
    // api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody CreateRegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Check thong tin nguoi dung hien tai, de xac dinh user, phan quyen, token, de viet logic cho navbar, auth.js, quan ly login
    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<CurrentUserDTO> getCurrentUser(HttpServletRequest request) {
        return ResponseEntity.ok(authService.getCurrentUser(request));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest rq, HttpServletResponse response) {

        //Buoc 1: Kiem tra username, password co dung khong?
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        rq.getUsername(),
                        rq.getPassword()));

        //Neu sai -> auto throw exception (BadCredentialsException)

        //Buoc 2: load user tu database
        UserDetails user = customUserDetailsService.loadUserByUsername(rq.getUsername());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String username = jwtService.extractUsername(accessToken);
        List<String> roles = jwtService.extractRoles(accessToken);

        // Tạo JWT Cookie tên là "accessToken"
        ResponseCookie cookie = ResponseCookie
                .from("accessToken", accessToken)

                // JavaScript không thể đọc cookie
                // Console tren Chrome document.cookie tra ve ""
                // Giúp giảm nguy cơ đánh cắp JWT qua XSS
                .httpOnly(true)

                // false: cho phép gửi cookie qua HTTP, phù hợp localhost khi development
                // Khi deploy HTTPS thật hoặc dùng ngrok HTTPS nên đổi thành true
                .secure(false)

                // Cookie có hiệu lực cho toàn bộ URL của website
                // Ví dụ: /, /admin/products, /api/products, ...
                .path("/")

                // Cookie hết hạn sau 30 phút
                .maxAge(Duration.ofMinutes(30))

                // Cookie được gửi trong các request điều hướng thông thường
                // Phù hợp với frontend/backend cùng site
                .sameSite("Lax")

                // Hoàn tất việc tạo cookie
                .build();

        // Tạo JWT Cookie tên là "refreshToken"
        // Tạo một Cookie có: Tên:   refreshToken  Giá trị: refreshToken
        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                //Chỉ gửi refreshToken Cookie khi request đi tới /api/auth/refresh (hoặc path con của nó)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        // Thêm cookie vào HTTP Response.
        // Sau khi login thành công, browser sẽ nhận và lưu cookie "accessToken".
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());


        //Buoc 3: tao token
        return Map.of(
                "username", username,
                "roles", roles

                //Check data
//                "accessToken",accessToken,
//                "refreshToken",refreshToken
        );


    }

    // POST /api/auth/refresh
    // Dùng refreshToken để tạo accessToken mới

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(

            // Lấy refreshToken từ Cookie của Browser
            @CookieValue(
                    value = "refreshToken",
                    required = false
            ) String refreshToken,

            // Dùng để gửi Cookie mới về Browser
            HttpServletResponse response) {

        // Không có refreshToken → không thể tạo accessToken mới
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token not found");
        }

        try {

            // Lấy username được lưu bên trong refreshToken
            String username = jwtService.extractUsername(refreshToken);

            // Tìm thông tin user từ Database và chuyển thành UserDetails, thì mới dùng được isTokenValid(param ở trong chú ý,
            // không được ép kiểu tuỳ tiện từ User sang UserDetails)‼️
            UserDetails user =
                    customUserDetailsService.loadUserByUsername(username);

            // Kiểm tra refreshToken:
            // - Username trong token khớp với user
            // - Token chưa hết hạn
            if (!jwtService.isTokenValid(refreshToken, user)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired refresh token");
            }

            // RefreshToken hợp lệ → tạo accessToken mới
            String newAccessToken =
                    jwtService.generateAccessToken(user);

            // Tạo Cookie chứa accessToken mới
            ResponseCookie accessTokenCookie =
                    ResponseCookie
                            .from("accessToken", newAccessToken)
                            .httpOnly(true)       // JS không đọc được
                            .secure(false)        // localhost
                            .path("/")            // Gửi cho toàn bộ API
                            .maxAge(Duration.ofMinutes(30))
                            .sameSite("Lax")
                            .build();

            // Gửi accessToken Cookie mới về Browser
            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    accessTokenCookie.toString()
            );

            // Refresh thành công
            return ResponseEntity.ok("Access token refreshed");

        } catch (Exception e) {

            // Token lỗi hoặc user không tồn tại → từ chối refresh
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid refresh token");
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessTokenCookie.toString()
        );

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        return ResponseEntity.ok("Logout successful");
    }
}

/*security/
  JwtService.java
  JwtFilter.java
  SecurityConfig.java
  CustomUserDetailsService.java

controller/
  AuthController.java

dto/
  LoginRequest.java*/
