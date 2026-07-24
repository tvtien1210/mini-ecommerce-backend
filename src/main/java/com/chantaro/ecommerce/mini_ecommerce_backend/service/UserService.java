package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.CreateUserRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.update.UpdateUserRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.user.UserDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Role;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.UserMapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.repository.RoleRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

// Đánh dấu đây là Service → chứa business logic
// Service クラスとして定義 → business logic を管理
@Service
public class UserService {

    // Repository để làm việc với DB
    // DB操作用 Repository
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection (Spring sẽ tự inject UserRepository vào)
    // Constructor Injection
    // Spring が自動的に Repository を inject する
    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // Hàm lấy toàn bộ user (API sẽ gọi xuống đây)
    // 全ユーザー取得処理（API から呼び出される）
    public List<UserDTO> getAllUsers() {

        return userRepository.findAll() // lấy tất cả User từ DB (List<User>)
                // DB から全 User を取得（List<User>）

                .stream() // chuyển List thành stream để xử lý từng phần tử
                // List を stream に変換して各要素を処理

                .map(user -> UserMapper.toDTO(user))
                // convert từng User → UserDTO
                // tương đương: user -> UserMapper.toDTO(user)

                // 各 User を UserDTO に変換
                // user -> UserMapper.toDTO(user) と同じ

                .toList();
        // gom lại thành List<UserDTO>

        // List<UserDTO> にまとめて返却
    }

    //Lấy user theo id
    // id で user を取得
    public UserDTO getUserById(Long id) {

        // Gọi repository để tìm user theo id (trả về Optional<User>)
        // Repository を使って id で user を検索（Optional<User> を返す）
        User user = userRepository.findById(id)

                // Nếu không tìm thấy → throw exception custom (UserNotFoundException)
                // Lambda () -> ... chỉ được gọi khi Optional rỗng

                // 見つからない場合 → custom exception を throw
                // Lambda は Optional が空の場合のみ実行される
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Convert Entity → DTO trước khi trả ra API
        // Tránh lộ password, roles, và tránh vòng lặp JSON

        // API返却前に Entity → DTO に変換
        // password や roles の露出防止、
        // JSON循環参照防止のため
        return UserMapper.toDTO(user);
    }

    public User saveUser(CreateUserRequest req) {

        // Tạo entity User mới
        // 新しい User Entity を作成
        User user = new User();

        user.setUsername(req.getUsername());

        // Encode password trước khi lưu DB
        // DB保存前に password を encode
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        user.setEmail(req.getEmail());

        user.setFullName(req.getFullName());

        //Tự động lưu phân quyển thành role_customer, tránh client tự động set thành role_admin 💀
        // 自動的に ROLE_CUSTOMER を付与
        // client 側から勝手に ROLE_ADMIN を設定されるのを防ぐ 💀
        Role role = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

        user.setRoles(Set.of(role));

        //save theo tầng (layer) User user = new User(), sẽ UserMapper ở Controller Post Method sau
        // Layer構成に従って save
        // Controller の POST Method 側で UserMapper を利用予定
        return userRepository.save(user);
    }

    public User updateUser(Long id, UpdateUserRequest rq) {

        // Tìm user theo id
        // id で user を検索
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setUsername(rq.getUsername());

        // Encode password mới
        // 新しい password を encode
        user.setPassword(passwordEncoder.encode(rq.getPassword()));

        user.setEmail(rq.getEmail());

        user.setFullName(rq.getFullName());

        // Lưu dữ liệu update xuống DB
        // 更新内容を DB に保存
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {

        // Tìm user cần xóa
        // 削除対象 user を検索
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Xóa user khỏi DB
        // DB から user を削除
        userRepository.delete(user);
    }
}


//    public Order getOrder(Long orderId) {
//
//        // Lấy order từ DB theo id
//        // Nếu không tồn tại → ném exception
//
//        // id で Order を DB から取得
//        // 存在しない場合 → exception を throw
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        // Lấy thông tin user hiện tại đang đăng nhập từ SecurityContext
//        // SecurityContext から現在ログイン中の user 情報を取得
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        // Lấy username của user đang login
//        // ログイン中 user の username を取得
//        String currentUser = auth.getName();
//
//        // Kiểm tra xem user có phải CUSTOMER không
//        // (dựa vào danh sách quyền - authorities)
//
//        // user が CUSTOMER 権限かどうか確認
//        // （authorities を利用）
//        boolean isCustomer = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
//
//        // Nếu là CUSTOMER → chỉ được xem order của chính mình
//        // So sánh username của owner order với user hiện tại
//
//        // CUSTOMER の場合、自分の order のみ閲覧可能
//        // order owner の username と現在 user を比較
//        if (isCustomer && !order.getUser().getUsername().equals(currentUser)) {
//
//            // Nếu không phải chủ order → từ chối truy cập
//            // order owner ではない場合 → access 拒否
//            throw new AccessDeniedException("Access denied");
//        }
//
//        // Nếu là STAFF hoặc ADMIN → không bị giới hạn
//        // hoặc CUSTOMER nhưng là chủ order → cho phép truy cập
//
//        // STAFF / ADMIN は制限なし
//        // または CUSTOMER でも owner ならアクセス許可
//        return order;
//    }


/*
//Code cơ bản:
// 基本コード

List<User> users = userRepository.findAll();
List<UserDTO> result = new ArrayList<>();

for (User user : users) {

    UserDTO dto = UserMapper.toDTO(user); // convert từng phần tử
    // 各 User を DTO に変換

    result.add(dto);
}

return result;


//Biểu thức lambda
// Lambda式

//nhận vào 1 user,trả về UserDTO
// 1つの user を受け取り UserDTO を返す
user -> {
    return UserMapper.toDTO(user);
}

//viet gon hon khi code 1 dong
// 1行で短く書く
user -> UserMapper.toDTO(user)


// .map(UserMapper::toDTO) giong voi //.map(user -> UserMapper.toDTO(user))
// .map(UserMapper::toDTO) は
// .map(user -> UserMapper.toDTO(user)) と同じ
*/