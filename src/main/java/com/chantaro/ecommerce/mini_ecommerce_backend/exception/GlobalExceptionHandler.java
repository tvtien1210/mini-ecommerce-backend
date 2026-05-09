package com.chantaro.ecommerce.mini_ecommerce_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
//Áp dụng cho tất cả controller → global exception handler
public class GlobalExceptionHandler {

    //@ExceptionHandler(UserNotFoundException.class) → nói với Spring: khi gặp UserNotFoundException, hãy chạy method này.

    // =======================
    // Handle custom exception: UserNotFoundException
    // =======================
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handlerUserNotFoundException(UserNotFoundException ex){
        //LinkedHashMap lưu thứ tự insert → JSON sẽ serialize theo thứ tự put vào map.
        Map<String,Object> body = new LinkedHashMap<>();

        // Thêm thời điểm lỗi xảy ra, giúp debug/log dễ dàng
        body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // HTTP status code tương ứng: 404 Not Found, truyền value() vào field "status"
        body.put("status", HttpStatus.NOT_FOUND.value());

        // Tên lỗi, có thể customize cho client hiển thị
        body.put("error","Not Found 🥹");

        // Thông báo chi tiết từ exception
        body.put("message",ex.getMessage());

        // ResponseEntity chứa body + HTTP status → Spring trả về client
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        // 🔹 Ví dụ: handle IllegalArgumentException, DataIntegrityViolationException, Exception chung...

    }

    // =======================
    // Nó chứa rất nhiều thông tin dư thừa mà người dùng không cần biết, nên cần validation ex này
    // Validation Exception Handler
    // =======================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Bắt lỗi validation từ @Valid (DTO request)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex){

        System.out.println("🔥 Validation handler chạy");
        // log nhanh để debug (prod nên dùng logger thay vì System.out)

        List<String> errors = ex.getBindingResult()
                // BindingResult (kết quả validate request)
                .getFieldErrors()
                // lấy danh sách lỗi theo field (username, price,...)
                .stream()
                // stream API (xử lý collection)
                .map(err -> err.getDefaultMessage())
                // lấy message đã định nghĩa trong annotation (@NotNull,...)
                .toList();

        return ResponseEntity.badRequest()
                // HTTP 400
                .body(
                        Map.of(
                                "timestamp", LocalDateTime.now()
                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                // thời gian xảy ra lỗi

                                "status", HttpStatus.BAD_REQUEST.value(),
                                // mã HTTP: 400

                                "errors", "Bad Request",
                                // loại lỗi tổng quát

                                "message", errors
                                // danh sách lỗi chi tiết từng field
                        )
                );
    }

    // =======================
    // Global Exception Handler (catch-all)
    // =======================
    @ExceptionHandler(Exception.class)
    // bắt tất cả exception chưa được handle riêng
    public ResponseEntity<Object> handlerAllException(Exception ex){

        ex.printStackTrace();
        // in stack trace để debug

        Map<String,Object> body = new LinkedHashMap<>();
        // LinkedHashMap giữ thứ tự key (đẹp JSON response)

        body.put("timestamp",
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        // thời điểm lỗi

        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        // HTTP 500

        body.put("error", "Internal Server Error");
        // tên lỗi chuẩn

        body.put("message", ex.getMessage());
        // message từ exception (có thể null hoặc không thân thiện user)

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        // trả response 500
    }

}