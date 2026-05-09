package com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCartItemRequest {
    @NotNull(message = "productId is not null")
    private Long productId;
    @NotNull(message = "quantity is not null")
    @Positive
    private Integer quantity;
}
