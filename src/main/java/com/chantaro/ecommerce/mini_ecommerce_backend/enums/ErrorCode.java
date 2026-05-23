package com.chantaro.ecommerce.mini_ecommerce_backend.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // =====================================================
    // COMMON / 共通エラー
    // =====================================================

    // Lỗi hệ thống phía server
    // サーバー内部エラー
    INTERNAL_SERVER_ERROR(
            5000,
            "Internal server error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    // Request không hợp lệ
    // 不正なリクエスト
    INVALID_REQUEST(
            4000,
            "Invalid request",
            HttpStatus.BAD_REQUEST
    ),


    // =====================================================
    // ORDER / 注文関連
    // =====================================================

    // Không tìm thấy đơn hàng
    // 注文データが存在しない
    ORDER_NOT_FOUND(
            4001,
            "Order not found",
            HttpStatus.NOT_FOUND
    ),

    // Trạng thái đơn hàng không hợp lệ
    // 注文ステータス不正
    INVALID_ORDER_STATUS(
            4002,
            "Invalid order status",
            HttpStatus.BAD_REQUEST
    ),

    // =====================================================
    // PAYMENT / 決済関連
    // =====================================================

    // Chữ ký VNPay không hợp lệ
    // VNPay署名検証失敗
    INVALID_SIGNATURE(
            5001,
            "Invalid VNPay signature",
            HttpStatus.BAD_REQUEST
    ),

    // Phiên thanh toán đã hết hạn
    // 決済期限切れ
    PAYMENT_EXPIRED(
            5002,
            "Payment expired",
            HttpStatus.BAD_REQUEST
    ),

    // Đơn hàng đã thanh toán thành công trước đó
    // 既に決済成功済み
    PAYMENT_ALREADY_SUCCESS(
            5003,
            "Payment already success",
            HttpStatus.BAD_REQUEST
    ),

    // =====================================================
    // PRODUCT / 商品関連
    // =====================================================

    // Sản phẩm hết hàng
    // 在庫切れ
    OUT_OF_STOCK(
            6001,
            "Product out of stock",
            HttpStatus.BAD_REQUEST
    ),

    PRODUCT_NOT_FOUND(
            6002,
            "Product not found",
            HttpStatus.NOT_FOUND
    ),

    // =====================================================
    // USER / ユーザー関連
    // =====================================================
    USER_NOT_FOUND(
            7001,
            "User not found",
            HttpStatus.NOT_FOUND
    ),
    UNAUTHORIZED(
            4010,
            "Unauthenticated",
            HttpStatus.UNAUTHORIZED
    ),

    // =====================================================
    // CART / カート関連
    // =====================================================

    CART_ITEM_NOT_FOUND(
            8001,
            "Cart item not found",
            HttpStatus.NOT_FOUND
    ),

    INVALID_QUANTITY(

            8002,

            "Invalid quantity",

            HttpStatus.BAD_REQUEST

    ),

    // Không có quyền truy cập tài nguyên
    // アクセス権限なし
    FORBIDDEN(
            4030,
            "Access denied",
            HttpStatus.FORBIDDEN
    ),
    ;

    // Mã lỗi nội bộ
    // 内部エラーコード
    private final int code;

    // Message trả về client
    // クライアント向けメッセージ
    private final String message;

    // HTTP Status tương ứng
    // HTTPステータス
    private final HttpStatus httpStatus;

    // Constructor enum
    // enumコンストラクタ
    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    // Getter mã lỗi
    // エラーコード取得
    public int getCode() {
        return code;
    }

    // Getter message
    // メッセージ取得
    public String getMessage() {
        return message;
    }

    // Getter HTTP status
    // HTTPステータス取得
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}