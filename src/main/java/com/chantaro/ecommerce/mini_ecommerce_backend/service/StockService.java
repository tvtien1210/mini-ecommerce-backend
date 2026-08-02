package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.*;
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
// トランザクション制御
    public void reserveStock(Order order) {

        // Duyệt toàn bộ sản phẩm trong Order
        // 注文商品のループ
        for (OrderItem orderItem : order.getOrderItems()) {

            // Lấy Product hiện tại từ Persistence Context
            // (đã được Hibernate quản lý)
            // Managed状態の商品取得
            Product product = orderItem.getProduct();

            // Số lượng còn có thể bán
            // = Tồn kho thực tế - Hàng đang được giữ
            // 販売可能在庫数を計算
            int available =
                    product.getStock() - product.getReservedStock();

            // Số lượng khách đặt mua
            // 注文数量取得
            int quantity = orderItem.getQuantity();

            // Không chấp nhận số lượng <= 0
            // 数量不正チェック
            if (quantity <= 0) {
                throw new BusinessException(ErrorCode.INVALID_QUANTITY);
            }

            // Kiểm tra tồn kho có đủ để giữ hàng không
            // 在庫不足チェック
            if (available < quantity) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            // Giữ trước số lượng hàng cho đơn hàng này
            // Ví dụ:
            // stock = 10
            // reservedStock = 3
            // quantity = 2
            //
            // Sau khi reserve:
            // stock = 10
            // reservedStock = 5
            // available = 5
            //
            // 注文分の在庫を一時確保
            product.setReservedStock(
                    product.getReservedStock() + quantity
            );

            // Không cần gọi save()
            // save()不要

            // Product đang ở trạng thái Managed
            // Managedエンティティ

            // Hibernate sẽ tự sinh câu lệnh UPDATE
            // khi Transaction được commit
            // Commit時に自動UPDATE
        }
    }

    @Transactional
    public void confirmReservedStock(Order order) {

        for (OrderItem item : order.getOrderItems()) {

            Product product = item.getProduct();

            product.setStock(
                    product.getStock() - item.getQuantity()
            );

            product.setReservedStock(
                    product.getReservedStock() - item.getQuantity()
            );
        }
    }

    @Transactional
    public void releaseReservedStock(Order order) {

        for (OrderItem item : order.getOrderItems()) {

            Product product = item.getProduct();

            product.setReservedStock(
                    product.getReservedStock() - item.getQuantity()
            );
        }
    }


}