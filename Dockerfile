# Sử dụng Java Development Kit (JDK) phiên bản 21
# Đây là môi trường Java dùng để build và chạy Spring Boot application
FROM eclipse-temurin:21-jdk


# Tạo và chuyển vào thư mục /app bên trong Docker container
# Từ đây các câu lệnh tiếp theo sẽ được thực hiện trong /app
WORKDIR /app


# Copy toàn bộ source code của project vào thư mục /app trong container
# Bao gồm pom.xml, mvnw, src/, ...
COPY . .


# Cấp quyền thực thi cho Maven Wrapper
# Nếu không có quyền này, ./mvnw có thể không chạy được trong Linux container
RUN chmod +x mvnw


# Build Spring Boot application bằng Maven
# clean: xóa kết quả build cũ
# package: compile + package thành file .jar
# -DskipTests: bỏ qua test trong quá trình Docker build
# Vì đã chạy ./mvnw test trước đó
RUN ./mvnw clean package -DskipTests


# Khai báo rằng application sẽ sử dụng port 8080
# Spring Boot application sẽ lắng nghe port này trong container
EXPOSE 8080


# Command được chạy khi container khởi động
# Chạy file .jar đã được Maven tạo ra trong thư mục target/
CMD ["java", "-jar", "target/mini-ecommerce-backend-0.0.1-SNAPSHOT.jar"]