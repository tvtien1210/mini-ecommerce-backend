package com.chantaro.ecommerce.mini_ecommerce_backend.repository;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.OrderItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    // Spring Data JPA sẽ tự generate query:
    // SELECT COUNT(*) > 0 FROM order_item WHERE product = ?
    boolean existsByProduct(Product product);

}
