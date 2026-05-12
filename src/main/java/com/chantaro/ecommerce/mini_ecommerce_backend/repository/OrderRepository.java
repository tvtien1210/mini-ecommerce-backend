package com.chantaro.ecommerce.mini_ecommerce_backend.repository;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.orderItems
            WHERE o.user = :user
            """)
    List<Order> findByUser(@Param("user") User user);

}
