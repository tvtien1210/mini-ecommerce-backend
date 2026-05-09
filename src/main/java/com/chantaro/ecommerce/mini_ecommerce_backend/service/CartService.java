package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart.CartDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CreateCartItemRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.UpdateCartItemRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.CartItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CartStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.CartMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CartItemRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CartRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.ProductRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    // ===============================
    // 1. GET MY CART
    // ===============================
    public CartDTO getMyCart() {
        // 1. Lấy user từ JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Tìm cart ACTIVE
        // 3. Nếu null → return cart(create)
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .user(user)
                    .status(CartStatusCode.ACTIVE)
                    .totalPrice(BigDecimal.ZERO)
                    .build();
            return cartRepository.save(newCart);
        });

        // 4. Convert sang DTO
        return CartMapper.toDTO(cart);
    }


    // ===============================
    // 2. ADD TO CART
    // ===============================
    @Transactional
    public CartDTO createCartItem(CreateCartItemRequest rq) {

        // 1. Lấy user từ SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        // -> Nếu đang đăng nhập -> tìm username
        String username = auth.getName();

        // 2. Tìm user
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Not found user"));

        // 3. Tìm cart ACTIVE của user
        // nếu chưa có → create new cart
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .user(user)
                    .status(CartStatusCode.ACTIVE)
                    .totalPrice(BigDecimal.ZERO)
                    .build();
            return cartRepository.save(newCart);
        });


        // 4. Tìm product
        Product product = productRepository.findById(rq.getProductId()).orElseThrow(() -> new RuntimeException("Not found product"));

        // 5. Check product đã và đang tồn tại trong cart, stream duyệt qua từng phần tử cartItem trong list CartItems
        // Optional: wrapper cho giá trị có thể tồn tại hoặc không, giúp tránh NullPointerException
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                //so sánh id của productId nằm trong cardItem của từng phần tử với productId vừa click thêm vào để filter() tìm phần tử phù hợp
                .filter(cartItem -> cartItem.getProduct().getId().equals(rq.getProductId()))
                //Lấy thằng đầu tiên tìm được
                .findFirst();

        // 6. Nếu tồn tại product → tăng quantity
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + rq.getQuantity();
            //check stock
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("Out of stock");
            }
            //existingItem.get() = item vừa được tìm thấy khi so sánh với rq.getProductId trong cart hiện tại
            cartItem.increaseQuantity(rq.getQuantity());

        } else {

            //7. Nếu chưa tồn tại product → tạo mới CartItem

            //check stock
            if (product.getStock() < rq.getQuantity()) {
                throw new RuntimeException("Out of stock");
            }

            //tao moi CartItem
            CartItem cartItem = new CartItem();
            // set cart, product, quantity
            cartItem.setProduct(product);
            cartItem.setQuantity(rq.getQuantity());
            cartItem.setPrice(product.getPrice());

            // 8. Add item vào cart luôn,
            // trong addItem đã có cartItem.setCart(this)
            // cart.getItems().add(item); nhưng sẽ dùng helper method cho gọn và tránh thiếu code
            cart.addItem(cartItem);
        }

        System.out.println("Cart items size: " + cart.getCartItems().size());

        // 9. Save cart
        // cartRepository.save(cart);
        Cart newCart = cartRepository.save(cart);

        // 10. return DTO
        return CartMapper.toDTO(newCart);
    }

    // ===============================
    // 3. UPDATE QUANTITY
    // ===============================
    //INPUT → VALIDATE → SECURITY → BUSINESS RULE → UPDATE → SAVE → RESPONSE
    @Transactional
    public CartDTO updateCartItem(Long id, UpdateCartItemRequest rq) {
        // 1. Tìm cartItem
        CartItem cartItem = cartItemRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found cart item"));

        // 2. Check item này thuộc user hiện tại đang đăng nhập hay không (security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Not found user by username = " + username));

        // Lấy cart
        Cart cart = cartItem.getCart();

        //401 Unauthorized: Hệ thống không biết bạn là ai (chưa đăng nhập).
        //403 Forbidden: Hệ thống biết bạn là ai,  nhưng bạn không có quyền truy cập vào tài nguyên đó

        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        // 3. Validate quantity > 0
        // @Valid ở controller rồi nhưng chưa đủ chặt, làm thêm cho chắc
        if (rq.getQuantity() <= 0) {
            throw new RuntimeException("quantity must be > 0");
        }

        // 4. Check stock, số lượng update quantity không được vượt quá stock
        if (cartItem.getProduct().getStock() < rq.getQuantity()) {
            throw new RuntimeException("Out of stock");
        }

        // 5. Update quantity
        cartItem.setQuantity(rq.getQuantity());

        // 6. Update total price trong cart
        cart.calculateTotalPrice();

        //7.Save
        cartItemRepository.save(cartItem);

        // 8. return cart DTO
        return CartMapper.toDTO(cartItem.getCart());
    }


    // ===============================
    // 4. REMOVE ITEM
    // ===============================
    @Transactional
    //SUCCESS hết → commit
    //FAIL 1 cái → rollback toàn bộ
    public void deleteCartItem(Long id) {

        // 1. Lấy CartItem từ DB theo id
        // Nếu không tồn tại → throw exception
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found cart item"));

        // 2. Lấy thông tin user hiện tại từ SecurityContext
        // (user đã được authenticate trước đó)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Tìm user trong DB
        // Nếu không tồn tại → throw exception
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Not found user"));

        // 4. Lấy Cart chứa CartItem này
        Cart cart = cartItem.getCart();

        // 5. Kiểm tra quyền sở hữu
        // Đảm bảo CartItem thuộc về đúng user hiện tại
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        // 6. Xoá CartItem khỏi database, dùng helper method trong cart
        //JPA có 2 thứ: 1. Database 2. Persistence Context (RAM), dùng helper có chứa remove method,
        // để xoá đồng thời 2 nơi, tránh ObjectDeletedException, đã xoá ở db rồi, nhưng chưa xoá ở Ram (Object)
        // Chỉ xoá đúng item cần xoá
            cart.removeItem(cartItem);

        // 8. Lưu lại Cart với totalPrice mới
        cartRepository.save(cart);
    }
}

/*
throw new NotFoundException(...)
throw new UnauthorizedException(...)
throw new ForbiddenException(...)
throw new BadRequestException(...)

Sau đó map bằng @ControllerAdvice
*/
