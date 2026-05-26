package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.CartItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //Xử lý trừ tồn kho cho toàn bộ cart
    // カート全体の在庫減算処理

    //Xử lý trừ tồn kho với cơ chế retry khi xảy ra optimistic locking ở method liên quan
    // 関連メソッドで Optimistic Locking が発生した場合に retry 可能な在庫減算処理

    //Chạy trong 1 transaction (atomic: tất cả thành công hoặc rollback hết)
    // 1つの Transaction 内で実行（atomic: 全成功または全 rollback）
    @Transactional
    public void processStock(Cart cart) {

        // Duyệt từng item trong giỏ hàng
        // カート内の各 item をループ処理
        for (CartItem cartItem : cart.getCartItems()) {

            // Lấy product mới nhất từ DB (quan trọng để đảm bảo stock current)
            // DB から最新の Product 情報を取得
            // （最新の在庫状態を保証するため重要）
            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // Nếu không tìm thấy product → ném exception
            // Product が存在しない場合 → Exception を throw

            int quantity = cartItem.getQuantity();

            // Kiểm tra quantity hợp lệ
            // quantity が有効かチェック
            if (quantity <= 0) {
                throw new BusinessException(ErrorCode.INVALID_QUANTITY);
            }

            // Kiểm tra tồn kho có đủ không
            // 在庫数が足りるかチェック
            if (product.getStock() < quantity) {

                // Không đủ hàng → fail toàn bộ transaction
                // 在庫不足の場合 → Transaction 全体を失敗させる
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            // Trừ tồn kho
            // 在庫を減算
            product.setStock(
                    product.getStock() - quantity
            );

            // Lưu lại DB
            // DB に保存
            productRepository.save(product);

            // Có @Version → nếu concurrent update → sẽ throw OptimisticLock exception
            // @Version がある場合、
            // 同時更新（concurrent update）が発生すると
            // OptimisticLock Exception が throw される

            //Điều kiện để @Version hoạt động đúng
            //@Version が正しく動作する条件

            //Entity phải được load từ DB (managed entity, tim product tu repository theo id)
            //Entity は DB から取得された managed entity である必要がある
            //（repository から id で取得した product など）

            //Version sẽ tự tăng khi entity được update và flush xuống DB
            //Entity 更新後、DB に flush される際に
            // version は自動で increment される
        }
    }
}