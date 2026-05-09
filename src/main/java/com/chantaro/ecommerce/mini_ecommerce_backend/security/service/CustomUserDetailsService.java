package com.chantaro.ecommerce.mini_ecommerce_backend.security.service;

// import entity và repository của bạn
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.RoleRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;

// import Spring Security
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service // đánh dấu đây là service để Spring quản lý
public class CustomUserDetailsService implements UserDetailsService {

    // repository để lấy user từ DB
    private final UserRepository userRepository;

    // constructor injection (Spring sẽ tự inject UserRepository)
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
    }

    // method quan trọng nhất: Spring gọi khi login
    // @Transactional fix user.getRoles LAZY: nhưng nhược điểm là -> user.getRoles() còn transaction → Hibernate tự query thêm,dễ bị: N+1 query problem, performance tệ mà không biết
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // tìm user trong DB theo username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        //
        System.out.println("USERNAME LOGIN: " + username);

        // convert User entity → UserDetails (cho Spring Security hiểu)
        return new org.springframework.security.core.userdetails.User(

                user.getUsername(),   // username dùng để login

                user.getPassword(),   // password đã mã hoá (BCrypt)

                // convert roles → quyền (authority)
                // biến Role trong DB → sang quyền mà Spring Security hiểu
                // với mỗi role → tạo 1 object new SimpleGrantedAuthority

                //user.getRoles() ❌ (mặc định Lazy tải chậm, lúc này session đã đóng nên không getRoles), đã fix lỗi tại UserRepository
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList()
        );
    }
}

/* Flow
User nhập username + password
Spring gọi loadUserByUsername(username)
lấy user từ DB
lấy password (đã encode)
so sánh bằng BCrypt
nếu đúng -> login thành công */