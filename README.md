# Mini E-Commerce Backend System

E-commerce backend system built with **Java 21 & Spring Boot**.

Implemented with:
- RESTful API
- JWT Authentication
- Spring Security
- JPA/Hibernate
- VNPay Payment Integration
- Transaction Management


## 🚀 Features

- User registration & login
- JWT authentication & authorization
- Product management
- Shopping cart
- Order management
- VNPay payment integration
- Stock reservation handling
- Global exception handling


## 🛠 Tech Stack

Backend:
- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate

Database:
- MySQL

Tools:
- Maven
- Git
- IntelliJ IDEA


## 🏗 Architecture

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```


## 🔐 Security

- JWT based authentication
- BCrypt password encryption
- Role based authorization


## 💳 Payment Flow

```
Order
 ↓
Payment
 ↓
VNPay
 ↓
IPN Callback
 ↓
Update Status
```


Implemented:
- HMAC SHA512 verification
- Amount validation
- Idempotent callback handling


## 🗄 Database

Main entities:

- User
- Role
- Product
- Category
- Cart
- Order
- Payment


Relationship:

```
User
 ├── Cart
 ├── Orders
 └── Roles

Order
 ├── OrderItems
 └── Payment
```


## ⚙️ Setup

Clone:

```bash
git clone https://github.com/tvtien1210/mini-ecommerce-backend.git
```


Configure:

```properties
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

jwt.secret=

vnp.tmn-code=
vnp.hash-secret=
```


Run:

```bash
./mvnw spring-boot:run
```


## 📌 Future Improvements

- React frontend
- Docker deployment
- CI/CD
- Cloud deployment


## 👨‍💻 Author

Chan Taro

GitHub:
https://github.com/tvtien1210