package com.chantaro.ecommerce.mini_ecommerce_backend.util;

import com.chantaro.ecommerce.mini_ecommerce_backend.config.VNPayConfig;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class VNPayUtilPractice {
    private final VNPayConfig vnPayConfig;

    //1.create payment url
    public String buildPaymentUrl(Order order, String txnRef, HttpServletRequest request) {
        try {

            String amount = String.valueOf(order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue());

            Map<String, String> params = new HashMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", vnPayConfig.getTmnCode());

            params.put("vnp_Amount", amount);
            params.put("vnp_CurrCode", "VND");

            params.put("vnp_TxnRef", txnRef);

            params.put("vnp_OrderInfo", "Thanh toán đơn hàng" + order.getId());
            params.put("vnp_OrderType", "other");

            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

            //IP client
            params.put("vnp_IpAddr", request.getRemoteAddr());

            //timestamp
            params.put("vnp_CreateDate", getCurrentTime());

            //SORT PARAMS: sap xep params theo thu tu

            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String field : fieldNames) {

                String value = params.get(field);
                if (value != null && !value.isEmpty()) {

                    if (hashData.length() > 0) {
                        hashData.append("&");
                        query.append("&");

                        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
                        hashData.append(field).append("=").append(encoded);
                        query.append(field).append("=").append(encoded);
                    }

                }
            }


            //HASH
            String secureHash = hmacSHA512Practice(vnPayConfig.getHashSecret(), hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            return vnPayConfig.getUrl()+"?"+query;

        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay URL",e);
        }
    }

    private String hmacSHA512Practice(String key, String data) {
        try {

            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            return bytesToHexPractice(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing", e);
        }
    }

    private String bytesToHexPractice(byte[] rawHmac) {
        StringBuilder hex = new StringBuilder(rawHmac.length * 2);

        for (byte b : rawHmac) {
            String s = Integer.toHexString(0xff & b);

            if (s.length() == 1) {
                hex.append('0');
            }
        }
        return hex.toString();
    }

    private String getCurrentTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter);
    }




}
