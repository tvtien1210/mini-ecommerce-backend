package com.chantaro.ecommerce.mini_ecommerce_backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter

public class VNPayConfig {
    private String tmnCode;
    private String hashSecret;
    private String url;
    private String returnUrl;
    private String ipnUrl;

//    @PostConstruct
//    public void checkConfig(){
//        System.out.println("TMN CODE = " + tmnCode);
//    }
}

//public class VNPayConfig {
//
//    // Mã merchant do VNPay cấp
//    // Dùng để định danh hệ thống của bạn khi gửi request thanh toán
//    public static String vnp_TmnCode = "MY_TMN_CODE";
//
//    // Secret key (cực kỳ quan trọng)
//    // Dùng để tạo hash (ký request) và verify dữ liệu từ VNPay trả về
//    // Không được lộ ra frontend hoặc commit public
//    public static String vnp_HashSecret = "YOUR_SECRET";
//
//    // URL cổng thanh toán VNPay
//    // Backend sẽ build URL đầy đủ (params + hash) rồi redirect user sang đây
//    // Sandbox: dùng để test (không mất tiền)
//    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//
//    // URL VNPay redirect user về sau khi thanh toán xong
//    // Dùng để hiển thị kết quả cho user (success / fail)
//    // Không nên dùng để update DB vì không đáng tin
//    public static String vnp_ReturnUrl = "http://localhost:8080/api/payment/return";
//
//    // IPN (Instant Payment Notification)
//    // VNPay sẽ gọi API này từ server của họ → backend của bạn
//    // Đây mới là nơi CHÍNH để update trạng thái payment trong DB
//    public static String vnp_IpnUrl = "http://localhost:8080/api/payment/ipn";
//}

//Dùng trong service
//@Autowired
//private VNPayConfig config;
//config.getTmnCode();