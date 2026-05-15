# Mini E-Commerce Backend System

Backend system for e-commerce platform built with Java and Spring Boot.  
Designed with RESTful API architecture and enterprise backend development practices.

Java & Spring Boot を用いて開発したE-Commerce向けバックエンドシステムです。  
RESTful API設計およびエンタープライズバックエンド開発を意識して実装しています。

Hệ thống backend thương mại điện tử được xây dựng bằng Java & Spring Boot.  
Dự án được thiết kế theo hướng RESTful API và enterprise backend development.

---

# 📌 Features | 主な機能 | Chức năng chính

- User authentication & authorization (JWT)  
  JWTを利用したユーザー認証・認可  
  Xác thực và phân quyền người dùng bằng JWT

- Product management  
  商品管理機能  
  Quản lý sản phẩm

- Cart management  
  カート管理機能  
  Quản lý giỏ hàng

- Order management  
  注文管理機能  
  Quản lý đơn hàng

- VNPay payment integration  
  VNPay決済システム連携  
  Tích hợp thanh toán VNPay

- Payment callback (IPN) handling  
  VNPayのIPNコールバック処理  
  Xử lý callback/IPN từ VNPay

- Reserved stock handling  
  決済中の在庫引当処理  
  Giữ trước số lượng sản phẩm trong quá trình thanh toán

- Transaction management  
  データ整合性を保証するトランザクション管理  
  Quản lý transaction đảm bảo tính toàn vẹn dữ liệu

- Exception handling  
  グローバル例外処理  
  Xử lý exception toàn hệ thống

- Payment timeout handling  
  決済タイムアウト処理  
  Tự động xử lý thanh toán hết hạn

- RESTful API design  
  RESTful API設計  
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
- Lombok
- VNPay Sandbox
- Git & GitHub

---

# 📂 Project Structure

```text
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
|-- util
```

---

# 🔗 API Endpoints

## 🏠 Home

| Method | Endpoint | Description |
|---|---|---|
| GET | / | Home page |

---

## 🔐 Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/login | User login & generate JWT token |

---

## 👤 Users

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/users | Get all users |
| GET | /api/users/{id} | Get user by id |
| POST | /api/users | Create new user |
| PUT | /api/users/{id} | Update user |
| DELETE | /api/users/{id} | Delete user |

---

## 📦 Products

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/products | Get all products |
| GET | /api/products/{id} | Get product by id |
| POST | /api/products | Create product |
| PUT | /api/products/{id} | Update product |
| DELETE | /api/products/{id} | Delete product |

---

## 🛒 Cart

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/cart/my | Get current user's cart |
| POST | /api/cart/items | Add item to cart |
| PUT | /api/cart/items/{id} | Update cart item quantity |
| DELETE | /api/cart/items/{id} | Remove item from cart |

---

## 📄 Orders

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/orders/my | Get current user's orders |
| POST | /api/orders/checkout | Checkout cart & create order |
| POST | /api/orders/{id}/pay | Mark order as paid |
| PATCH | /api/orders/{id}/status | Update order status |
| DELETE | /api/orders/{id} | Delete order |
| DELETE | /api/orders/{orderId}/items/{itemId} | Remove item from order |

---

## 💳 Payments (VNPay)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/payment/{orderId}/vnpay | Create VNPay payment URL |
| GET | /api/payment/return | VNPay return URL |
| GET | /api/payment/ipn | VNPay IPN callback |

---

# 🔒 VNPay Security Handling

VNPay integration includes the following security mechanisms:

- HMAC SHA512 signature verification
- IPN callback validation
- Idempotency handling
- Amount verification

VNPay決済では以下のセキュリティ対策を実装しています。

- HMAC SHA512による署名検証
- IPNコールバック検証
- 冪等性（Idempotency）制御
- 決済金額検証

VNPay được triển khai với các cơ chế bảo mật:

- Xác minh chữ ký HMAC SHA512
- Xác thực callback IPN
- Xử lý idempotency
- Kiểm tra số tiền thanh toán

---

# 🗄 Database Schema

## Main Entities

- User
- Role
- Product
- Category
- Cart
- CartItem
- Order
- OrderItem
- Payment

---

# 🔗 Relationships

## User ↔ Role

- Many Users → One Role
- 1つのRoleに複数のUserが属する
- Một role có thể có nhiều user

## User ↔ Cart

- One User → One Cart
- 1ユーザーにつき1つのカートを保持
- Mỗi user có một giỏ hàng riêng

## Cart ↔ CartItem

- One Cart → Many CartItems
- 1つのカートに複数の商品を保持
- Một giỏ hàng chứa nhiều sản phẩm

## Product ↔ CartItem

- One Product → Many CartItems
- 1つの商品が複数のCartItemに存在可能
- Một sản phẩm có thể nằm trong nhiều cart item

## Category ↔ Product

- One Category → Many Products
- 1つのカテゴリに複数の商品を所属
- Một danh mục chứa nhiều sản phẩm

## User ↔ Order

- One User → Many Orders
- 1ユーザーが複数の注文を作成可能
- Một user có thể tạo nhiều đơn hàng

## Order ↔ OrderItem

- One Order → Many OrderItems
- 1つの注文に複数の商品を保持
- Một đơn hàng chứa nhiều sản phẩm

## Product ↔ OrderItem

- One Product → Many OrderItems
- 1つの商品が複数のOrderItemに存在可能
- Một sản phẩm có thể xuất hiện trong nhiều order item

## Order ↔ Payment

- One Order → Many Payments
- 1つの注文に対して複数回の決済を許可
- Một order có thể có nhiều lần thanh toán (retry payment)

---

# 📌 Payment Design

Payment is separated from Order to support:

- Payment retry
- Payment history
- Traceability
- VNPay transaction tracking
- Idempotent callback handling

決済情報をOrderから分離することで以下を実現しています。

- 決済失敗時のリトライ対応
- 決済履歴管理
- トランザクション追跡性向上
- txnRefによるVNPay取引追跡
- 重複コールバック防止処理

Payment được tách riêng khỏi Order để hỗ trợ:

- Thanh toán lại khi giao dịch thất bại
- Lưu lịch sử thanh toán
- Theo dõi và truy vết transaction
- Theo dõi transaction VNPay qua txnRef
- Tránh xử lý callback nhiều lần

---

# 📌 Inventory Handling

System supports:

- Stock quantity management
- Reserved stock management
- Prevent overselling during payment process
- Auto release reserved stock when payment failed/expired

システムでは以下の在庫管理機能をサポートしています。

- 実在庫管理
- 決済待機中の在庫引当
- 在庫超過販売防止
- 決済失敗・期限切れ時の在庫自動解放

Hệ thống hỗ trợ:

- Quản lý tồn kho thực tế
- Giữ trước số lượng sản phẩm
- Tránh oversell trong quá trình thanh toán
- Tự động trả lại reserved stock khi thanh toán thất bại hoặc hết hạn

---

# 🔐 JWT Authentication Flow

## Flow

1. User login
2. Backend generates JWT token
3. Client stores token
4. Client sends token in Authorization header

### 日本語

1. ユーザーログイン
2. バックエンドでJWT生成
3. クライアント側でトークン保存
4. AuthorizationヘッダーにJWTを付与してAPIアクセス

### Tiếng Việt

1. Người dùng đăng nhập
2. Backend tạo JWT token
3. Client lưu token
4. Client gửi token qua Authorization header

---

## Example

```http
Authorization: Bearer access_token
```

---

# 💳 VNPay Flow

1. Client creates order
2. Backend generates VNPay payment URL
3. User redirects to VNPay
4. VNPay redirects user to return URL
5. VNPay calls IPN endpoint
6. Backend verifies:
  - signature
  - amount
  - response code
7. Backend updates payment & order status

---

## 日本語

1. クライアントが注文作成
2. バックエンドでVNPay決済URL生成
3. ユーザーをVNPayへリダイレクト
4. VNPayからReturn URLへリダイレクト
5. VNPayがIPN Endpointを呼び出し
6. バックエンド側で検証
  - 署名検証
  - 金額検証
  - レスポンスコード検証
7. 決済状態・注文状態更新

---

## Tiếng Việt

1. Client tạo order
2. Backend tạo VNPay payment URL
3. Người dùng được chuyển sang VNPay
4. VNPay redirect về return URL
5. VNPay gọi IPN endpoint
6. Backend kiểm tra:
  - chữ ký
  - số tiền
  - mã phản hồi
7. Backend cập nhật trạng thái payment và order

---

# 🔒 Security Handling

- HMAC SHA512
- Signature verification
- Idempotency handling
- Amount verification

---

## 日本語

- HMAC SHA512署名
- 署名検証
- 冪等性制御
- 金額検証

---

## Tiếng Việt

- HMAC SHA512
- Xác minh chữ ký
- Xử lý idempotency
- Kiểm tra số tiền

---

# 🚀 How To Run

## Clone project

```bash
git clone https://github.com/tvtien1210/mini-ecommerce-backend.git
```

---

## Configure environment variables

```env
VNP_TMN_CODE=tmn_code
VNP_HASH_SECRET=hash_secret
JWT_SECRET=jwt_secret
```

---

# ⚙️ Environment Variables

| Variable | Description |
|---|---|
| VNP_TMN_CODE | VNPay terminal code |
| VNP_HASH_SECRET | VNPay secret key |
| JWT_SECRET | JWT signing secret |

---

# 👨‍💻 Author

Chan Taro

## GitHub

[mini-ecommerce-backend repository](https://github.com/tvtien1210/mini-ecommerce-backend)