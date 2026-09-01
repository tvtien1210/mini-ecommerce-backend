package com.chantaro.ecommerce.mini_ecommerce_backend.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

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
    public String generateAccessToken(UserDetails user) {

        return Jwts.builder()

                // set "subject" = username
                // JWT sẽ lưu username vào payload
                .setSubject(user.getUsername())

                // thêm thông tin roles vào token (custom field)
                // ví dụ: ROLE_USER, ROLE_ADMIN
                .claim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())

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
    public String generateRefreshToken(UserDetails user) {

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
    public String extractUsername(String token) {

        // parse token (giải mã + verify chữ ký)
        return Jwts.parserBuilder()

                // dùng SECRET để kiểm tra chữ ký
                // nếu sai → throw exception
                .setSigningKey(key)

                .build()
                // parse JWT (header + payload + signature)
                .parseClaimsJws(token)

                // lấy phần payload (body)
                .getBody()

                // lấy subject (username)
                .getSubject();
    }

    //LẤY ROLE TỪ TOKEN
    public List extractRoles(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", List.class );
    }

    // Kiểm tra token có hợp lệ hay không
    // Điều kiện:
    // 1. Username trong token phải trùng với user trong DB
    // 2. Token chưa hết hạn
    public boolean isTokenValid(String token, UserDetails user) {

        return extractUsername(token).equals(user.getUsername())
                && !isTokenExpired(token);
    }


    // Kiểm tra token đã hết hạn chưa
    // Nếu thời gian hết hạn nhỏ hơn thời điểm hiện tại
    // => token hết hạn
    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    // Lấy thời gian hết hạn (exp) từ JWT
    // JWT chứa các thông tin như:
    // sub : username
    // exp : expiration time
    // roles : ROLE_ADMIN,...
    // Method này parse JWT rồi lấy trường exp

    //Để gọi hàm này từ AuthService, nó không được để private
        public Date extractExpiration(String token) {

        return Jwts.parserBuilder()

                // dùng secret key để verify chữ ký JWT
                .setSigningKey(key)

                .build()

                // parse JWT
                .parseClaimsJws(token)

                // lấy phần payload (Claims)
                .getBody()

                // lấy trường expiration
                .getExpiration();
    }
}