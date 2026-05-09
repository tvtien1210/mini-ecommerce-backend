package com.chantaro.ecommerce.mini_ecommerce_backend.exception;

// ===== Import =====
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
// Annotation này đánh dấu exception này khi bị throw sẽ tự động trả về HTTP status 404
// Không cần phải handle thủ công trong controller
public class UserNotFoundException extends RuntimeException {

    // Constructor nhận id của user không tồn tại
    public UserNotFoundException(Long id){
        // Message sẽ được trả về trong response hoặc log
        super("User not found with id: " + id + " 🥶");
    }

}