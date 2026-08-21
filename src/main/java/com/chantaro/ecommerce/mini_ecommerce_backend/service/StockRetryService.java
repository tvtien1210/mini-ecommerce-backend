package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class StockRetryService {

    private final StockService stockService;

    public StockRetryService(StockService stockService) {
        this.stockService = stockService;
    }


    // =====================================================
    // RESERVE STOCK WITH RETRY
    // Checkout → giữ hàng tạm thời
    // 楽観ロック失敗時にリトライ
    // =====================================================
    public void reserveStockWithRetry(Order order) {

        int maxRetry = 3;       // Số lần thử tối đa
        int attempt = 0;        // Số lần đã thử

        // Retry loop
        while (attempt < maxRetry) {

            try {

                // Giữ stock
                stockService.reserveStock(order);

                // Thành công → kết thúc
                return;

            } catch (ObjectOptimisticLockingFailureException e) {

                // Optimistic Lock thất bại → retry
                attempt++;

                // Đã thử đủ số lần → báo hệ thống bận
                if (attempt >= maxRetry) {

                    throw new BusinessException(
                            ErrorCode.SYSTEM_BUSY
                    );
                }

                try {

                    // Chờ trước khi retry để giảm xung đột
                    Thread.sleep(100);

                } catch (InterruptedException ex) {

                    // Khôi phục trạng thái interrupt của thread
                    Thread.currentThread().interrupt();

                    throw new BusinessException(
                            ErrorCode.SYSTEM_BUSY
                    );
                }
            }
        }
    }


    // =====================================================
    // CONFIRM STOCK WITH RETRY
    // VNPay SUCCESS → trừ stock thật
    // 楽観ロック失敗時にリトライ
    // =====================================================
    public void processStockWithRetry(Order order) {

        int maxRetry = 3;       // Số lần thử tối đa
        int attempt = 0;        // Số lần đã thử

        // Retry loop
        while (attempt < maxRetry) {

            try {

                // Trừ stock thật và giảm reservedStock
                stockService.confirmReservedStock(order);

                // Thành công → kết thúc
                return;

            } catch (ObjectOptimisticLockingFailureException e) {

                // Optimistic Lock thất bại → retry
                attempt++;

                // Đã thử đủ số lần → báo hệ thống bận
                if (attempt >= maxRetry) {

                    throw new BusinessException(
                            ErrorCode.SYSTEM_BUSY
                    );
                }

                try {

                    // Chờ trước khi retry để giảm xung đột
                    Thread.sleep(100);

                } catch (InterruptedException ex) {

                    // Khôi phục trạng thái interrupt của thread
                    Thread.currentThread().interrupt();

                    throw new BusinessException(
                            ErrorCode.SYSTEM_BUSY
                    );
                }
            }
        }
    }
}