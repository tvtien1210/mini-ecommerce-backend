package com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

//JwtService = công cụ xử lý JWT
@Service
public class JwtService {

    // 👉 Tạo key bí mật dùng để ký JWT
    // secret: chuỗi bí mật của bạn (ví dụ: "my-secret-key-123456...")
    // Keys.hmacShaKeyFor(...) sẽ convert secret thành dạng Key chuẩn cho thuật toán HMAC

    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    // TẠO ACCESS TOKEN (thời gian ngắn: 15 phút)
    public String generateAccessToken(UserDetails user){

        return Jwts.builder()

                // set "subject" = username
                // JWT sẽ lưu username vào payload
                .setSubject(user.getUsername())

                // thêm thông tin roles vào token (custom field)
                // ví dụ: ROLE_USER, ROLE_ADMIN
                .claim("roles", user.getAuthorities().stream().map(auth->auth.getAuthority()).toList())

                // set thời gian hết hạn (15 phút)
                // nếu quá hạn → token không dùng được
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))

                // ký token bằng thuật toán HS256 + SECRET
                // nếu SECRET sai → token invalid
                .signWith(key)

                // build thành chuỗi JWT hoàn chỉnh
                .compact();
    }


    // TẠO REFRESH TOKEN (thời gian dài: 7 ngày)
    public String generateRefreshToken(UserDetails user){

        return Jwts.builder()

                // vẫn lưu username
                .setSubject(user.getUsername())

                // thời gian sống dài hơn (7 ngày)
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))

                // ký token
                .signWith(key)

                // build token
                .compact();
    }


    //  LẤY USERNAME TỪ TOKEN
    public String extractUsername(String token){

        // parse token (giải mã + verify chữ ký)
        return Jwts.parser()

                // dùng SECRET để kiểm tra chữ ký
                // nếu sai → throw exception
                .setSigningKey(key)

                // parse JWT (header + payload + signature)
                .parseClaimsJws(token)

                // lấy phần payload (body)
                .getBody()

                // lấy subject (username)
                .getSubject();
    }
}