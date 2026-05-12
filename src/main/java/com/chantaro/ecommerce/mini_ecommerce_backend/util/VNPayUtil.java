package com.chantaro.ecommerce.mini_ecommerce_backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.chantaro.ecommerce.mini_ecommerce_backend.config.VNPayConfig;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class VNPayUtil {

    private final VNPayConfig vnPayConfig;

    // =========================
    // 1. CREATE PAYMENT URL
    // =========================
    public String buildPaymentUrl(Order order, String txnRef, HttpServletRequest request) {

        try {


            // VNPay yêu cầu amount * 100
            // KHÔNG có dấu thập phân
            // Không dùng .toString() trực tiếp
            // Dùng .longValue() trước để BigDecimal ví dụ, 10000.00 sẽ thành long 1000000(bỏ đi dấu phẩy ở giữa, nếu không sẽ sai giá trị amount gửi lên vnpay)
            String amount = String.valueOf(order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue());

            //request payload gửi sang VNPay
            Map<String, String> params = new HashMap<>();

            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay"); //lenh: pay
            params.put("vnp_TmnCode", vnPayConfig.getTmnCode());

            params.put("vnp_Amount", amount);
            params.put("vnp_CurrCode", "VND"); //currency, tiền tệ

            params.put("vnp_TxnRef", txnRef);

            params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getId());
            params.put("vnp_OrderType", "other");

            params.put("vnp_Locale", "vn"); //locale: hiển thị giao diện cho người dùng bằng tiếng Việt (vn) hay tiếng Anh (en)

            params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());

            // IP client
            params.put("vnp_IpAddr", request.getRemoteAddr()); //(Máy cá nhân): getRemoteAddr() thường trả về 0:0:0:0:0:0:0:1 hoặc 127.0.0.1. VNPay Sandbox chấp nhận giá trị này để test.

            // 🔥 timestamp
            params.put("vnp_CreateDate", getCurrentTime());

            params.put("vnp_ExpireDate", getExpireTime());

            // =========================
            // SORT PARAMS
            // =========================
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder(); //hashData = "" lúc này empty, length =0: khoá làm bằng params data
            StringBuilder query = new StringBuilder(); // chuỗi query = "": để đính chữ ký bảo mật vào Url

            for (String field : fieldNames) {
                //String value = params.get("vnp_Amount");
                //value = "50000000"
                String value = params.get(field);

                if (value != null && !value.isEmpty()) {

                    if (hashData.length() > 0) {
                        hashData.append("&");
                        query.append("&");
                    }

                    //ex : value sau khi encode = "50000000"
                    //ex : value sau khi encode = "thanh + toan + don +  hang + 1001"
                    String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);

                    hashData.append(field).append("=").append(encoded);
                    query.append(field).append("=").append(encoded);
                }
            }

            // =========================
            // HASH
            // =========================
            // secret key + data → tạo chữ ký bảo mật : secureHash xác minh tính toàn vẹn
            String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            // đính chữ ký bảo mật vào request để VNPay verify
            query.append("&vnp_SecureHash=").append(secureHash);

            return vnPayConfig.getUrl() + "?" + query;

        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay URL", e);
        }
    }

    // =========================
    // 2. VERIFY SIGNATURE (IPN)
    // =========================
    public boolean verify(Map<String, String> params) {

        try {
            // 🔥 lấy hash từ VNPay
            String receivedHash = params.get("vnp_SecureHash");

            // remove để build lại hash
            Map<String, String> clone = new HashMap<>(params);
            clone.remove("vnp_SecureHash");
            clone.remove("vnp_SecureHashType");

            // build lại string
            List<String> fieldNames = new ArrayList<>(clone.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            for (String field : fieldNames) {
                String value = clone.get(field);

                if (value != null && !value.isEmpty()) {

                    if (hashData.length() > 0) {
                        hashData.append("&");
                    }

                    hashData.append(field)
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                }
            }

            // hash lại
            String calculatedHash = hmacSHA512(
                    vnPayConfig.getHashSecret(),
                    hashData.toString()
            );

            return calculatedHash.equals(receivedHash);

        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // 3. HMAC SHA512
    // =========================
    public String hmacSHA512(String key, String data) {

        try {

            // Tạo object HMAC với thuật toán SHA512
            Mac mac = Mac.getInstance("HmacSHA512");


            // Convert secret key String -> byte[]
            // rồi tạo SecretKeySpec cho HMAC
            SecretKeySpec secretKey = new SecretKeySpec(

                    // convert String key thành bytes UTF-8
                    key.getBytes(StandardCharsets.UTF_8),

                    // thuật toán dùng
                    "HmacSHA512"
            );


            // Gắn secret key vào object HMAC
            mac.init(secretKey);


            // Hash dữ liệu:
            // data + secret key
            // => tạo ra mảng bytes hash
            byte[] rawHmac = mac.doFinal(

                    // convert data thành bytes
                    data.getBytes(StandardCharsets.UTF_8)
            );


            // Convert bytes hash -> hex string
            // ví dụ:
            // [12, 55, -1]
            // =>
            // "0c37ff"
            return bytesToHex(rawHmac);

        } catch (Exception e) {

            // Nếu lỗi:
            // - sai thuật toán
            // - key invalid
            // - lỗi encode
            // => throw runtime exception
            throw new RuntimeException("Error while hashing", e);
        }
    }

    // =========================
    // 4. BYTE → HEX
    // =========================
    private static String bytesToHex(byte[] bytes) {

        // Tạo StringBuilder để chứa chuỗi hex result
        // mỗi byte sẽ thành 2 ký tự hex
        // nên capacity = bytes.length * 2
        StringBuilder hex = new StringBuilder(bytes.length * 2);


        // Duyệt từng byte trong mảng bytes
        for (byte b : bytes) {

            // Convert byte -> hex string
            //
            // Java byte -128 -> 127
            // 0xff & b:
            // tránh bị số âm khi convert byte sang int
            //
            // Ví dụ:
            // byte = -1
            // => 255
            //
            // Integer.toHexString(255)
            // => "ff"
            String s = Integer.toHexString(0xff & b);


            // Nếu hex chỉ có 1 ký tự
            // thì thêm số 0 phía trước
            //
            // Ví dụ:
            // "a"
            // => "0a"
            //
            // để đảm bảo mỗi byte luôn có 2 ký tự hex
            if (s.length() == 1) {
                hex.append('0');
            }


            // Append hex của byte vào result
            hex.append(s);
        }


        // Convert StringBuilder -> String
        return hex.toString();
    }

    // =========================
    // 5. TIME FORMAT VNPay
    // =========================
    private String getCurrentTime() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        return LocalDateTime.now().format(formatter);
    }

    private String getExpireTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        return LocalDateTime.now().plusMinutes(15).format(formatter);
    }
}

//1.
/*Client click "Thanh toán"

→ Backend tạo request gửi sang VNPay
    → amount
    → order info
    → txnRef
    → returnUrl
    → timestamp
    → hash bảo mật

→ Backend tạo URL

→ Redirect user sang VNPay




-----

1. Generate transaction info
2. Build params
3. Encode + sort params
4. Generate secure hash
5. Build final payment URL



------

VNPayService
 ├── createTxnRef()
 ├── buildParams()
 ├── buildHashData()
 ├── generateSecureHash()
 └── buildPaymentUrl()

 -------
 1. payment attempt != order

2. external API luôn cần:
   - validate
   - encode
   - sign/hash

3. payment system luôn cần:
   - idempotency
   - security
   - traceability

4. clean backend =
   tách từng responsibility


   ------

1. Authentication
   → đúng merchant không?
   vnp_TmnCode

2. Integrity
   → dữ liệu bị sửa chưa?
   vnp_SecureHash

3. Traceability
   → transaction nào?
   vnp_TxnRef
 */
