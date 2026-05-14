# Mini E-Commerce Backend System

Backend system for e-commerce platform built with Java and Spring Boot.  
Hệ thống backend thương mại điện tử được xây dựng bằng Java & Spring Boot theo hướng RESTful API và enterprise backend development.

---

# 📌 Features | Chức năng chính

- User authentication & authorization  
  Xác thực và phân quyền người dùng bằng JWT

- Product management  
  Quản lý sản phẩm

- Shopping cart  
  Giỏ hàng

- Order management  
  Quản lý đơn hàng

- VNPay payment integration  
  Tích hợp thanh toán VNPay

- Payment callback (IPN) handling  
  Xử lý callback/IPN từ VNPay

- Transaction management  
  Quản lý transaction đảm bảo tính toàn vẹn dữ liệu

- Exception handling  
  Xử lý exception toàn hệ thống

- RESTful API design  
  Thiết kế API theo chuẩn RESTful

---

# 🛠 Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- Maven
- JWT Authentication
- VNPay Sandbox
- Git & GitHub

---

# 📂 Project Structure

src/main/java  

|-- config
|-- controller
|-- dto
|-- entity
|-- enums
|-- exception
|-- mapper
|-- repository
|-- security
|-- service
`-- util

---

# 🔐 Authentication & Security

- JWT-based authentication
- Role-based authorization
- Secure password encryption
- HMAC SHA512 signature verification
- Idempotent callback handling

Hệ thống sử dụng JWT để xác thực người dùng và verify chữ ký bảo mật từ VNPay nhằm đảm bảo tính an toàn khi xử lý thanh toán.

---

# 💳 Payment Flow | Luồng thanh toán VNPay

Integrated with VNPay payment gateway.

## Payment Process

1. User creates order
2. System generates VNPay payment URL
3. User completes payment on VNPay
4. VNPay redirects user to return URL
5. VNPay sends IPN callback to backend
6. Backend verifies secure hash
7. Order & payment status updated

## Security Handling

- HMAC SHA512 signature verification
- Idempotent callback handling
- Transaction management
- Payment validation

Project tập trung xử lý payment flow thực tế như verify chữ ký, callback nhiều lần (idempotent) và transaction handling.

---

# 🧪 API Example

## Create Payment URL

POST /api/payment/vnpay 

## VNPay Return URL

GET /api/payment/vnpay-return 

## VNPay IPN Callback

GET /api/payment/vnpay-ipn

---

# ⚙️ Database Design

## Main Entities

- Cart
- CartItem
- Category
- Order
- OrderItem
- Payment
- Product
- Role
- User

Hệ thống được thiết kế theo mô hình entity relationship của một nền tảng thương mại điện tử cơ bản.

---

# 🚀 Getting Started

## Clone Project

bash git clone https://github.com/tvtien1210/mini-ecommerce-backend.git 

## Configure Database

properties

spring.application.name=Mini Ecommerce Backend  
spring.datasource.url=jdbc:mysql://localhost:3306/shop_db 
spring.datasource.username=shopuser spring.datasource.password=shoppass 

## Run Project

bash mvn spring-boot:run 

---

# 📖 Learning Goals

This project was built to practice:

- Backend architecture
- Payment gateway integration
- REST API design
- Transaction handling
- Secure coding practices
- Enterprise backend development

Dự án được thực hiện nhằm nâng cao kỹ năng backend Java theo hướng enterprise application và tích hợp thanh toán thực tế.

---

# 👨‍💻 Author

Forward Tran

GitHub:  
https://github.com/tvtien1210/mini-ecommerce-backe
