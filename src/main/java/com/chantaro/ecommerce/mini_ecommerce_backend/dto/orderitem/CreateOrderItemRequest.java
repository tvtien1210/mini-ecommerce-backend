package com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderitem;

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
public class CreateOrderItemRequest {
    //Sản phẩm gì?
    @NotNull(message = "productId is not null")
    private Long productId;

    //Số lượng bao nhiêu?
    @NotNull(message = "quantity is not null")
    @Positive
    private Integer quantity;
}
/*Tư duy tổng quát

Bước 1:

API này dùng để làm gì?
	Create OrderItem

Bước 2:

Client cần nhập gì?
	productId
	quantity

Bước 3:

Field nào KHÔNG cho client nhập?
	price
	total
	status

Bước 4:

Validate gì?
	not null
	0
*/