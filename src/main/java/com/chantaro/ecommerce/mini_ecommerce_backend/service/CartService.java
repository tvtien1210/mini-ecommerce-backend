package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart.CartDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CreateCartItemRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.UpdateCartItemRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.CartItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.CartStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
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
import java.net.BindException;
import java.util.Optional;

@Service
// カート関連業務ロジックサービス
public class CartService {

    // Repository層
    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private ProductRepository productRepository;
    private UserRepository userRepository;

    @Autowired
    // コンストラクタインジェクション
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    // ===============================
    // 1. GET MY CART
    // ===============================
    // 現在ログイン中ユーザーのカート取得
    public CartDTO getMyCart() {

        // 1. Lấy user từ JWT
        // Spring Security認証情報取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 未認証チェック
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String username = auth.getName();

        // ユーザー情報取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. Tìm cart ACTIVE
        // 有効カート検索

        // 3. Nếu null → return cart(create)
        // カート未存在の場合は新規作成
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE).orElseGet(() -> {

            // 新規カート生成
            Cart newCart = Cart.builder()
                    .user(user)
                    .status(CartStatusCode.ACTIVE)
                    .totalPrice(BigDecimal.ZERO)
                    .build();

            // DB保存（永続化）
            return cartRepository.save(newCart);
        });

        // 4. Convert sang DTO
        // DTO変換
        return CartMapper.toDTO(cart);
    }


    // ===============================
    // 2. ADD TO CART
    // ===============================
    @Transactional
    // トランザクション制御
    // 途中失敗時は自動ロールバック
    public CartDTO createCartItem(CreateCartItemRequest rq) {

        // 1. Lấy user từ SecurityContext
        // 認証ユーザー取得
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 未認証チェック
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // -> Nếu đang đăng nhập -> tìm username
        // ログインユーザー名取得
        String username = auth.getName();

        // 2. Tìm user
        // DBからユーザー検索
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. Tìm cart ACTIVE của user
        // 有効カート取得

        // nếu chưa có → create new cart
        // 未存在時は新規作成
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatusCode.ACTIVE).orElseGet(() -> {

            // カート新規生成
            Cart newCart = Cart.builder()
                    .user(user)
                    .status(CartStatusCode.ACTIVE)
                    .totalPrice(BigDecimal.ZERO)
                    .build();

            return cartRepository.save(newCart);
        });


        // 4. Tìm product
        // 商品情報取得
        Product product = productRepository.findById(rq.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // 5. Check product đã và đang tồn tại trong cart
        // カート内同一商品チェック

        // stream duyệt qua từng phần tử cartItem trong list CartItems
        // Stream APIでCartItem一覧走査

        // Optional: wrapper cho giá trị có thể tồn tại hoặc không
        // OptionalでNull安全対応
        Optional<CartItem> existingItem = cart.getCartItems().stream()

                //so sánh id của productId nằm trong cardItem của từng phần tử với productId vừa click thêm vào để filter() tìm phần tử phù hợp
                // 商品ID一致チェック
                .filter(cartItem -> cartItem.getProduct().getId().equals(rq.getProductId()))

                //Lấy thằng đầu tiên tìm được
                // 最初の一致データ取得
                .findFirst();

        // 6. Nếu tồn tại product → tăng quantity
        // 既存商品なら数量加算
        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            int newQuantity = cartItem.getQuantity() + rq.getQuantity();

            //check stock
            // 在庫チェック
            if (product.getStock() < newQuantity) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            //existingItem.get() = item vừa được tìm thấy khi so sánh với rq.getProductId trong cart hiện tại
            // 数量更新
            cartItem.increaseQuantity(rq.getQuantity());

        } else {

            //7. Nếu chưa tồn tại product → tạo mới CartItem
            // 新規CartItem作成

            //check stock
            // 在庫不足チェック
            if (product.getStock() < rq.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            //tao moi CartItem
            // CartItemエンティティ生成
            CartItem cartItem = new CartItem();

            // set cart, product, quantity
            // 商品・数量設定
            cartItem.setProduct(product);
            cartItem.setQuantity(rq.getQuantity());
            cartItem.setPrice(product.getPrice());

            // 8. Add item vào cart luôn,
            // カートへ商品追加

            // trong addItem đã có cartItem.setCart(this)
            // 双方向関連付け設定済み

            // cart.getItems().add(item); nhưng sẽ dùng helper method cho gọn và tránh thiếu code
            // helper method利用で整合性維持
            cart.addItem(cartItem);
        }

        // デバッグログ
        System.out.println("Cart items size: " + cart.getCartItems().size());

        // 9. Save cart
        // カート保存

        // cartRepository.save(cart);
        Cart newCart = cartRepository.save(cart);

        // 10. return DTO
        // DTO返却
        return CartMapper.toDTO(newCart);
    }

    // ===============================
    // 3. UPDATE QUANTITY
    // ===============================

    //INPUT → VALIDATE → SECURITY → BUSINESS RULE → UPDATE → SAVE → RESPONSE
    // 入力 → バリデーション → 認可 → 業務ロジック → 更新 → 保存 → レスポンス
    @Transactional
    public CartDTO updateCartItem(Long id, UpdateCartItemRequest rq) {

        // 1. Tìm cartItem
        // CartItem取得
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 2. Check item này thuộc user hiện tại đang đăng nhập hay không (security)
        // 認可チェック
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 未認証チェック
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String username = auth.getName();

        // ユーザー取得
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Lấy cart
        // カート取得
        Cart cart = cartItem.getCart();

        //401 Unauthorized: Hệ thống không biết bạn là ai (chưa đăng nhập).
        //403 Forbidden: Hệ thống biết bạn là ai,  nhưng bạn không có quyền truy cập vào tài nguyên đó

        // 認可エラーチェック
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN); // khong co quyen truy cap tai nguyen
        }

        // 3. Validate quantity > 0
        // 数量バリデーション

        // @Valid ở controller rồi nhưng chưa đủ chặt, làm thêm cho chắc
        // サービス層でも業務バリデーション実施
        if (rq.getQuantity() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }

        // 4. Check stock, số lượng update quantity không được vượt quá stock
        // 在庫超過チェック
        if (cartItem.getProduct().getStock() < rq.getQuantity()) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // 5. Update quantity
        // 数量更新
        cartItem.setQuantity(rq.getQuantity());

        // 6. Update total price trong cart
        // 合計金額再計算
        cart.calculateTotalPrice();

        //7.Save
        // DB保存
        cartItemRepository.save(cartItem);

        // 8. return cart DTO
        // DTO返却
        return CartMapper.toDTO(cartItem.getCart());
    }


    // ===============================
    // 4. REMOVE ITEM
    // ===============================
    @Transactional

    //SUCCESS hết → commit
    //FAIL 1 cái → rollback toàn bộ

    // 全件成功時コミット
    // 途中失敗時ロールバック
    public void deleteCartItem(Long id) {

        // 1. Lấy CartItem từ DB theo id
        // IDからCartItem取得

        // Nếu không tồn tại → throw exception
        // 未存在時例外発生
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        // 2. Lấy thông tin user hiện tại từ SecurityContext
        // 認証ユーザー情報取得

        // (user đã được authenticate trước đó)
        // 認証済みユーザー
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. Tìm user trong DB
        // DBからユーザー取得

        // Nếu không tồn tại → throw exception
        // 未存在時例外
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4. Lấy Cart chứa CartItem này
        // Cart取得
        Cart cart = cartItem.getCart();

        // 5. Kiểm tra quyền sở hữu
        // 所有者チェック

        // Đảm bảo CartItem thuộc về đúng user hiện tại
        // ログインユーザー所有確認
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 6. Xoá CartItem khỏi database, dùng helper method trong cart
        // CartItem削除

        //JPA có 2 thứ: 1. Database 2. Persistence Context (RAM), dùng helper có chứa remove method,
        // JPA永続化コンテキスト同期

        // để xoá đồng thời 2 nơi, tránh ObjectDeletedException, đã xoá ở db rồi, nhưng chưa xoá ở Ram (Object)
        // DBとメモリ両方同期削除

        // Chỉ xoá đúng item cần xoá
        // 対象商品のみ削除
        cart.removeItem(cartItem);

        // 8. Lưu lại Cart với totalPrice mới
        // 合計金額更新保存
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