package com.chantaro.ecommerce.mini_ecommerce_backend.dto.order;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderitem.CreateOrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    // Ai đặt hàng (userId -> sẽ được gọi qua order.user, nhưng đã qua authentication nên không cần field này nữa, tránh bị fake userId truyền vào )
    // private Long userId;

    //Đặt những gì?
    @NotEmpty(message = "Order items list is not null")
    @Valid //validate từng phần tử trong list
    private List<CreateOrderItemRequest> orderItems = new ArrayList<>();
}
