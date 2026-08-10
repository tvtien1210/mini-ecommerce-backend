package com.chantaro.ecommerce.mini_ecommerce_backend.exception;

import com.chantaro.ecommerce.mini_ecommerce_backend.exception.response.ApiResponse;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // Business Exception Handler
    // 業務例外処理
    // =====================================================

    // - ORDER_NOT_FOUND
    // - OUT_OF_STOCK
    // - INVALID_SIGNATURE
    //
    // 発生例:
    // - 注文未存在
    // - 在庫不足
    // - 署名不正
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(
            BusinessException ex
    ) {

        // Lấy ErrorCode được lưu bên trong BusinessException
        // BusinessException内部のErrorCode取得
        ErrorCode errorCode = ex.getErrorCode();

        // Trả response tương ứng với ErrorCode
        // ErrorCodeに応じたレスポンス返却
        return ResponseEntity

                // Set HTTP Status
                // HTTPステータス設定
                .status(errorCode.getHttpStatus())

                // Set response body
                // レスポンスBody設定
                .body(
                        ApiResponse.error(

                                errorCode.getCode(),

                                errorCode.getMessage()
                        )
                );
    }

    // =====================================================
    // Validation Exception Handler
    // バリデーション例外処理
    // =====================================================

    // Ví dụ:
    // @NotBlank
    // @Email
    // @Size
    //
    // 発生例:
    // 必須チェック
    // メール形式チェック
    // 桁数チェック
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        // Lấy toàn bộ validation errors
        // 全バリデーションエラー取得
        List<String> errors = ex.getBindingResult()

                // Lấy field errors
                // フィールドエラー取得
                .getFieldErrors()

                // Convert sang stream để xử lý
                // Stream変換
                .stream()

                // Lấy message của từng validation error
                // 各エラーメッセージ取得
                .map(fieldError -> fieldError.getDefaultMessage())

                // Convert thành List<String>
                // List<String>へ変換
                .toList();

        return ResponseEntity

                // HTTP 400 Bad Request
                // HTTP400設定
                .badRequest()

                // Response body
                // レスポンスBody
                .body(
                        ApiResponse.error(

                                // Error code cho invalid request
                                // 不正リクエストエラーコード
                                ErrorCode.INVALID_REQUEST.getCode(),

                                // Convert list error thành String
                                // エラー一覧文字列変換
                                errors.toString()
                        )
                );
    }


    //BadCredentialsException Exception Handler
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex) {

        //Vì đây là Exception bắt được từ Spring Security nên cần phải xử lý riêng
        //Không giống Business Exception, được bắt qua OrElseThrow trực tiếp từ funtion

        ErrorCode errorCode = ErrorCode.INVALID_USERNAME_PASSWORD; //giong voi ex.errorCode goi tu trong funtion qua orelsethrow
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // =====================================================
    // Common Exception Handler
    // 共通例外処理
    // =====================================================

    // Ví dụ:
    // - NullPointerException
    // - ArithmeticException
    // - RuntimeException
    // - Database Exception
    //
    // 発生例:
    // - NullPointerException
    // - ゼロ除算
    // - DBエラー
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(
            Exception ex
    ) {

        // In stacktrace ra console để debug
        // デバッグ用スタックトレース出力
        ex.printStackTrace();

        return ResponseEntity

                // HTTP 500 Internal Server Error
                // HTTP500設定
                .status(HttpStatus.INTERNAL_SERVER_ERROR)

                // Response body
                // レスポンスBody設定
                .body(
                        ApiResponse.error(

                                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),

                                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                        )
                );
    }
}

/*ResponseEntity
        |
        |
        HTTP response


ApiResponse
        |
        |
        Body JSON trả về*/