package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.OrderItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {


    // RESERVE STOCK
    // Giữ trước hàng khi khách checkout.
    // stock không giảm ngay.
    // reservedStock tăng lên.
    @Transactional
    public void reserveStock(Order order) {

        // Duyệt toàn bộ sản phẩm trong Order
        for (OrderItem orderItem : order.getOrderItems()) {

            // Lấy Product từ OrderItem
            Product product = orderItem.getProduct();

            // Số lượng còn có thể bán
            int available =
                    product.getStock()
                            - product.getReservedStock();

            // Số lượng khách muốn mua
            int quantity =
                    orderItem.getQuantity();


            // Không chấp nhận quantity <= 0
            if (quantity <= 0) {

                throw new BusinessException(
                        ErrorCode.INVALID_QUANTITY
                );
            }


            // Kiểm tra tồn kho
            if (available < quantity) {

                throw new BusinessException(
                        ErrorCode.OUT_OF_STOCK
                );
            }


            // Giữ trước số lượng hàng
            product.setReservedStock(
                    product.getReservedStock() + quantity
            );
        }
    }

    // RESERVE STOCK WITH RETRY
    // 楽観ロック失敗時のリトライ処理
    @Transactional
    public void reserveStockWithRetry(Order order) {

        int maxRetry = 3;
        int attempt = 0;

        while (attempt < maxRetry) {

            try {

                // Thực hiện giữ stock
                // 在庫確保
                reserveStock(order);

                // Thành công → kết thúc
                return;

            } catch (ObjectOptimisticLockingFailureException e) {

                // Optimistic Lock thất bại
                // 楽観ロック失敗
                attempt++;

                // Đã retry đủ số lần
                if (attempt >= maxRetry) {

                    throw new BusinessException(
                            ErrorCode.SYSTEM_BUSY
                    );
                }

                try {

                    // Chờ 100ms trước khi retry
                    Thread.sleep(100);

                } catch (InterruptedException ex) {

                    // Giữ lại trạng thái interrupt của thread
                    Thread.currentThread().interrupt();

                    throw new BusinessException(
                            ErrorCode.SYSTEM_BUSY
                    );
                }
            }
        }
    }


    // CONFIRM RESERVED STOCK
    // Khách thanh toán thành công.
    // stock giảm.
    // reservedStock giảm.
    @Transactional
    public void confirmReservedStock(Order order) {

        for (OrderItem item : order.getOrderItems()) {

            Product product =
                    item.getProduct();

            int quantity =
                    item.getQuantity();


            // Trừ tồn kho thật
            product.setStock(
                    product.getStock() - quantity
            );


            // Bỏ trạng thái giữ hàng
            product.setReservedStock(
                    product.getReservedStock() - quantity
            );
        }
    }


    // RELEASE RESERVED STOCK
    // Khách không thanh toán hoặc payment hết hạn.
    // Không trừ stock thật.
    // Chỉ trả lại reservedStock.
    @Transactional
    public void releaseReservedStock(Order order) {

        for (OrderItem item : order.getOrderItems()) {

            Product product =
                    item.getProduct();

            int quantity =
                    item.getQuantity();


            // Giải phóng số hàng đã reserve
            product.setReservedStock(
                    product.getReservedStock() - quantity
            );
        }
    }
}