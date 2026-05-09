package com.chantaro.ecommerce.mini_ecommerce_backend.service;

// import DTO (dữ liệu trả ra API)

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.UserNotFoundException;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.CreateUserRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.update.UpdateUserRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.UserDTO;

// import Mapper (convert Entity → DTO)
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.UserMapper;

// import Repository (truy vấn DB)
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.RoleRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

// Đánh dấu đây là Service → chứa business logic
@Service
public class UserService {

    // Repository để làm việc với DB
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection (Spring sẽ tự inject UserRepository vào)
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // Hàm lấy toàn bộ user (API sẽ gọi xuống đây)
    public List<UserDTO> getAllUsers() {

        return userRepository.findAll() // lấy tất cả User từ DB (List<User>)

                .stream() // chuyển List thành stream để xử lý từng phần tử

                .map(user -> UserMapper.toDTO(user))
                // convert từng User → UserDTO
                // tương đương: user -> UserMapper.toDTO(user)

                .toList();
        // gom lại thành List<UserDTO>
    }

    //Lấy user theo id
    public UserDTO getUserById(Long id) {

        // Gọi repository để tìm user theo id (trả về Optional<User>)
        User user = userRepository.findById(id)

                // Nếu không tìm thấy → throw exception custom (UserNotFoundException)
                // Lambda () -> ... chỉ được gọi khi Optional rỗng
                .orElseThrow(() -> new UserNotFoundException(id));

        // Convert Entity → DTO trước khi trả ra API
        // Tránh lộ password, roles, và tránh vòng lặp JSON
        return UserMapper.toDTO(user);
    }

    public User saveUser(CreateUserRequest req) {
        User user = new User();

        user.setUsername(req.getUsername());

        user.setPassword(passwordEncoder.encode(req.getPassword()));

        user.setEmail(req.getEmail());

        user.setFullName(req.getFullName());

        //Tự động lưu phân quyển thành role_customer, tránh client tự động set thành role_admin 💀
        Role role = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Set.of(role));

        //save theo tầng (layer) User user = new User(), sẽ UserMapper ở Controller Post Method sau
        return userRepository.save(user);
    }

    public User updateUser(Long id, UpdateUserRequest rq) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setUsername(rq.getUsername());
        user.setPassword(passwordEncoder.encode(rq.getPassword()));
        user.setEmail(rq.getEmail());
        user.setFullName(rq.getFullName());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }



}


//    public Order getOrder(Long orderId) {
//
//        // Lấy order từ DB theo id
//        // Nếu không tồn tại → ném exception
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        // Lấy thông tin user hiện tại đang đăng nhập từ SecurityContext
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        // Lấy username của user đang login
//        String currentUser = auth.getName();
//
//        // Kiểm tra xem user có phải CUSTOMER không
//        // (dựa vào danh sách quyền - authorities)
//        boolean isCustomer = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
//
//        // Nếu là CUSTOMER → chỉ được xem order của chính mình
//        // So sánh username của owner order với user hiện tại
//        if (isCustomer && !order.getUser().getUsername().equals(currentUser)) {
//            // Nếu không phải chủ order → từ chối truy cập
//            throw new AccessDeniedException("Access denied");
//        }
//
//        // Nếu là STAFF hoặc ADMIN → không bị giới hạn
//        // hoặc CUSTOMER nhưng là chủ order → cho phép truy cập
//        return order;
//    }


/*
//Code cơ bản:
List<User> users = userRepository.findAll();
List<UserDTO> result = new ArrayList<>();
for (User user : users) {
    UserDTO dto = UserMapper.toDTO(user); // convert từng phần tử
    result.add(dto);
}
return result;

//Biểu thức lambda
//nhận vào 1 user,trả về UserDTO
user -> {
    return UserMapper.toDTO(user);
}
//viet gon hon khi code 1 dong
user -> UserMapper.toDTO(user)

// .map(UserMapper::toDTO) giong voi //.map(user -> UserMapper.toDTO(user))
*/