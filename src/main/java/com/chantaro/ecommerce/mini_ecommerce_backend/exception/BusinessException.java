package com.chantaro.ecommerce.mini_ecommerce_backend.exception;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;

// Exception dùng cho business logic
// 業務ロジック用例外クラス
public class BusinessException extends RuntimeException {

    // Thông tin mã lỗi business
    // 業務エラーコード
    private final ErrorCode errorCode;

    // Constructor nhận ErrorCode
    // ErrorCode受け取りコンストラクタ
    public BusinessException(ErrorCode errorCode) {

        // Set message cho RuntimeException
        // RuntimeExceptionへメッセージ設定
        super(errorCode.getMessage());

        this.errorCode = errorCode;
    }

    // Getter lấy ErrorCode
    // ErrorCode取得
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}