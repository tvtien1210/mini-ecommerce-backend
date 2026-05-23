package com.chantaro.ecommerce.mini_ecommerce_backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {

    // Trạng thái thành công hay thất bại
    // 処理成功フラグ
    private boolean success;

    // Mã response nội bộ
    // 内部レスポンスコード
    private int code;

    // Message trả về client
    // クライアント向けメッセージ
    private String message;

    // Dữ liệu response
    // レスポンスデータ
    private T data;

    // Constructor đầy đủ tham số
    // 全項目コンストラクタ
    public ApiResponse(
            boolean success,
            int code,
            String message,
            T data
    ) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Constructor rỗng
    // デフォルトコンストラクタ
    public ApiResponse() {
    }

    // Tạo success response
    // 成功レスポンス生成
    public static <T> ApiResponse<T> success(T data) {

        ApiResponse<T> response = new ApiResponse<>();

        response.setSuccess(true);
        response.setMessage("Success");
        response.setData(data);

        return response;
    }

    // Tạo error response
    // エラーレスポンス生成
    public static <T> ApiResponse<T> error(
            int code,
            String message
    ) {

        ApiResponse<T> response = new ApiResponse<>();

        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);

        return response;
    }
}