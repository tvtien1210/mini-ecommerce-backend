package com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CartItemDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CreateCartItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCartRequest {
    @NotNull(message = "Cart items list is not null")
    @Valid
    List<CreateCartItemRequest> cartItems = new ArrayList<>();
}
