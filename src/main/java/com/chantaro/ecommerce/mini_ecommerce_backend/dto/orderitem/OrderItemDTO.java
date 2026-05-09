package com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderitem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long productId; // find product by productId
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}

/*	Có productId → để frontend biết sản phẩm nào
	Có productName → để hiển thị
	Có quantity → số lượng
	Có price → giá tại thời điểm mua (quan trọng!)

	------
	Giá (price) lấy từ đâu?
        Không phải từ Product hiện tại (giá hiện tại có thể đã bị cập nhật)
        Mà từ OrderItem.price (snap giá từ thời điểm click mua)

*/