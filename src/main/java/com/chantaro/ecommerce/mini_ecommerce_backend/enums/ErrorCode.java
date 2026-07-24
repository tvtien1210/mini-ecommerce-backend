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

    // Trạng thái đơn hàng hiện tại không tồn tại
    // 現在の注文ステータスが存在しない
    ORDER_STATUS_MISSING(
            4003,
            "Current order has no status",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    // Trạng thái mới trùng với trạng thái hiện tại
    // 新しいステータスが現在のステータスと同じ
    ORDER_STATUS_ALREADY_SET(
            4004,
            "The status is already the same",
            HttpStatus.BAD_REQUEST
    ),

    // Chuyển trạng thái đơn hàng không hợp lệ
    // 不正な注文ステータス遷移
    INVALID_ORDER_STATUS_TRANSITION(
            4005,
            "Invalid order status transition",
            HttpStatus.BAD_REQUEST
    ),

    // Không tìm thấy sản phẩm trong đơn hàng
    // 注文商品のデータが存在しない
    ORDER_ITEM_NOT_FOUND(
            4006,
            "Order item not found",
            HttpStatus.NOT_FOUND
    ),

    // Sản phẩm không thuộc đơn hàng này
    // 商品がこの注文に属していない
    ORDER_ITEM_NOT_BELONG_TO_ORDER(
            4007,
            "Order item does not belong to this order",
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

    // Hệ thống đang bận, vui lòng thử lại sau
    // システムが混雑しています。後でもう一度お試しください
    SYSTEM_BUSY(
            5004,
            "System busy, please try again later",
            HttpStatus.SERVICE_UNAVAILABLE
    ),

    // Số tiền thanh toán không khớp
    // 決済金額不一致
    INVALID_PAYMENT_AMOUNT(
            5005,
            "Invalid payment amount",
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

    // Sản phẩm đang được sử dụng
    // 商品は使用中のため削除不可
    PRODUCT_IN_USE(
            6003,
            "Cannot delete because product is being used",
            HttpStatus.BAD_REQUEST
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

    // Giỏ hàng trống
    // カートが空です
    CART_EMPTY(
            8003,
            "Cart is empty",
            HttpStatus.BAD_REQUEST
    ),

    // Không có quyền truy cập tài nguyên
    // アクセス権限なし
    FORBIDDEN(
            4030,
            "Access denied",
            HttpStatus.FORBIDDEN
    ),

    // =====================================================
    // CATEGORY / カテゴリ関連
    // =====================================================

    // Không tìm thấy category
    // カテゴリが存在しない
    CATEGORY_NOT_FOUND(
            9001,
            "Category not found",
            HttpStatus.NOT_FOUND
    ),
    // =====================================================
    // ROLE / ロール関連
    // =====================================================

    // Không tìm thấy role
    // ロールが存在しない
    ROLE_NOT_FOUND(
            10001,
            "Role not found",
            HttpStatus.NOT_FOUND
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