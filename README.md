# Chantaro Store

A mini e-commerce application built to practice Java backend development with Spring Boot.

## Tech Stack

- **Java 21**
- **Spring Boot**
- **Spring Data JPA / Hibernate**
- **Spring Security**
- **JWT Authentication**
- **MySQL**
- **REST API**
- **Thymeleaf / Bootstrap / JavaScript**
- **Maven**
- **Docker**
- **Railway**
- **VNPay Sandbox**

## Backend

- Layered architecture: Controller → Service → Repository
- Entity / DTO / Mapper separation
- Global exception handling with custom business errors
- Authentication and authorization with Spring Security and JWT
- HttpOnly Cookie-based authentication with access/refresh tokens
- JPA relationships and transaction management
- Environment variables for sensitive configuration
- REST API documented with Swagger / OpenAPI

## Business Logic

### Cart & Order

- One user can have an active cart.
- Before creating a new order, the system compares the current cart with the existing pending order.
- If changes are detected, it generates a new pending order; if not, it reuses the existing one.
- Product stock is reserved during checkout and released when payment fails or expires.
- Cart is marked as `CHECKED_OUT` only after successful payment.

### VNPay Payment

Payment flow:

```text
Checkout
   ↓
Create / Reuse Pending Order
   ↓
Create Payment
   ↓
VNPay
   ↓
VNPay IPN
   ↓
Verify Signature & Amount
   ↓
Update Payment / Order
   ↓
PAID
```

The application uses VNPay IPN as the server-side source for updating payment status.


## Deployment

- Docker image for application deployment
- Railway for cloud deployment
- Configuration managed through environment variables
- `develop` branch for testing
- `main` branch for production

## Project

**GitHub:** [Chantaro Store](https://github.com/tvtien1210/mini-ecommerce-backend)

**Live Demo:** [Chantaro Store](https://mini-ecommerce-backend-production-69d1.up.railway.app/)

> This project is a personal learning project focused on understanding how a Java/Spring Boot backend works in a real application, from authentication and database design to order processing, payment integration, and deployment.