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


    //shouldNotFilter() là method của OncePerRequestFilter.
    //Tránh Spring Security vẫn gọi JwtFilter trước, sau đó mới tới bước Authorization.
    //Ví dụ như ipn không cần filter mà filter vẫn gọi -> filter không catch(sẽ thành ExpiredJwtException) -> request chết luôn
    //Nếu trả về true -> Spring không gọi doFilterInternal() nữa.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/payment/ipn")
                || path.equals("/api/payment/return")
                || path.startsWith("/api/auth/");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //lay header Authorization (vi du Authorization : Bearer alfj-adlfn-andl), thi code nay se lay duoc doan tu Bearer tro di
        String header = request.getHeader("Authorization");
        //Kiem tra xem co token hay khong, va chuoi token co Bearer dung dau khong
        if (header != null && header.startsWith("Bearer ")) {
            //neu co (true) lay token, (cat 7 ky substring Bearer+daucach)
            String token = header.substring(7);
            try {

                // Lấy username từ JWT
                String username = jwtService.extractUsername(token);

                // Chỉ authenticate nếu request chưa có Authentication
                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Lấy thông tin user mới nhất từ Database
                    UserDetails user =
                            customUserDetailsService.loadUserByUsername(username);

                    // Kiểm tra token:
                    // - Username trong token có khớp với DB không
                    // - Token còn hạn sử dụng hay không
                    if (jwtService.isTokenValid(token, user)) {

                        //null là vì case này token của user này vẫn còn hạn, nên chỉ cần xác thực qua user + token thôi, không cần password nữa nên pw = null
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }

                //Dùng try catch để:

                //Nếu token còn hạn code sẽ dừng tại đây
                //---------

                //Nếu token hết hạn, sẽ catch và chạy tiếp filterChain.doFilter(request, response);
                //Lúc này SecurityContext không có Authentication, Request này chưa đăng nhập, Spring sẽ tự trả:401 Unauthorized
                //Tránh trường hợp JWT hết hạn là request sẽ trả về 500 Internal Server Error (lỗi máy chủ nội bộ) thay vì 401 Unauthorized (chưa xác thực)
            } catch (Exception e) {
                // Token không hợp lệ thì bỏ qua.
                // Không throw -> tránh lộ thông tin
            }
        }
        filterChain.doFilter(request, response);
    }


}

