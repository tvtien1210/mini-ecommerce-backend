package com.chantaro.ecommerce.mini_ecommerce_backend.dto.auth.payment;

import com.chantaro.ecommerce.mini_ecommerce_backend.enums.PaymentStatusCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private Long paymentId;
    private PaymentStatusCode paymentStatusCode;
    private String paymentUrl;

}
