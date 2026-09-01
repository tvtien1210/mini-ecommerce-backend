package com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt;

import com.chantaro.ecommerce.mini_ecommerce_backend.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtFilter(
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    // Những request này không cần JWT
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/payment/ipn")
                || path.equals("/api/payment/return")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")

                // Frontend public files
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Token ban đầu chưa có
        String token = null;

        // Ưu tiên lấy JWT từ Authorization Header
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        // Nếu không có Header → lấy JWT từ Cookie
        // Hiện tại chưa lấy được token từ Header → thử tìm token trong Cookie.
        if (token == null || token.isBlank()) {

            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {

                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // Không có JWT → cho request đi tiếp
        // Sau khi đã thử cả Header và Cookie, nếu vẫn không có token thì bỏ qua việc xác thực JWT.
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            // Lấy username từ JWT
            String username = jwtService.extractUsername(token);

            // Nếu chưa có Authentication → xác thực user
            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Lấy user từ Database
                UserDetails user =
                        customUserDetailsService.loadUserByUsername(username);

                // Kiểm tra JWT hợp lệ và còn hạn
                if (jwtService.isTokenValid(token, user)) {

                    // Tạo Authentication từ user + authorities
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );

                    // Lưu Authentication vào SecurityContext
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);
                }
            }

        } catch (Exception e) {

            // JWT không hợp lệ / hết hạn → bỏ qua
            // SecurityContext vẫn không có Authentication
        }

        // Cho request đi tiếp đến filter tiếp theo
        filterChain.doFilter(request, response);
    }
}