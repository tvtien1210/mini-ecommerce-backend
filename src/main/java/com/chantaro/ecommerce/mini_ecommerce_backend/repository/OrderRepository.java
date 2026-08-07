package com.chantaro.ecommerce.mini_ecommerce_backend.repository;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.OrderStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // DISTINCT : riêng biệt, "Loại bỏ dữ liệu trùng lặp" ví dụ mỗi order có mỗi item khác nhau

    //Lấy ra danh sách các Order không bị trùng, đồng thời lấy luôn các OrderItem thuộc từng Order, với điều kiện Order đó thuộc về user được truyền vào
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            JOIN FETCH o.orderItems
            WHERE o.user = :user
            """)
    List<Order> findByUser(@Param("user") User user);


    //Lấy tất cả Order không bị trùng, đồng thời lấy luôn OrderItem và User của từng Order
    @Query("""
                SELECT DISTINCT o
                FROM Order o
                JOIN FETCH o.orderItems
                JOIN FETCH o.user
            """)
    List<Order> getAllOrders();

    //Order 1 ---- N Payment
    //Method để Kiểm tra xem đã có Order PENDING chưa
    Optional<Order> findByUserAndStatus(User user, OrderStatusCode statusCode);

}
