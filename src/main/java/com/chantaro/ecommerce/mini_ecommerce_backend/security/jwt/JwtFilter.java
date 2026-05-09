package com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt;

import com.chantaro.ecommerce.mini_ecommerce_backend.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//JwtFilter = filter kiểm tra JWT trong mỗi request, dùng JwtService để decode + validate, sau đó set user vào SecurityContext.
@Component
public class JwtFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public JwtFilter(JwtService jwtService, CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //lay header Authorization (vi du Authorization : Bearer alfj-adlfn-andl), thi code nay se lay duoc doan tu Bearer tro di
        String header = request.getHeader("Authorization");
        //Kiem tra xem co token hay khong, va chuoi token co Bearer dung dau khong
        if (header != null && header.startsWith("Bearer ")) {
            //neu co (true) lay token, (cat 7 ky substring Bearer+daucach)
            String token = header.substring(7);
            // Lay username tu token
            // Mặc dù Token là hợp lệ về mặt chữ ký, nhưng Server vẫn cần kiểm tra xem User đó còn tồn tại trong hệ thống hay không.
            String username = jwtService.extractUsername(token);
            // check username + chưa authenticate
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user tu db
                UserDetails user = customUserDetailsService.loadUserByUsername(username);
                // Tao object xac thuc, nap quyen han (Authorities/Roles)
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                // Set thông tin user vào Spring Security (SecurityContext) setAuthentication(auth), vd Anh nay la Admin, cho anh ay vao,"Đã xác thực" (Authenticated)
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
