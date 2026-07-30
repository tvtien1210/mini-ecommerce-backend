package com.chantaro.ecommerce.mini_ecommerce_backend.dto.checkout;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CheckoutDTO {

    private Long orderId;

    private Long paymentId;

    private OrderStatusCode orderStatus;

    private PaymentStatusCode paymentStatus;

    private String paymentUrl;
}
