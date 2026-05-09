package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.CartItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.ProductRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class StockService {
    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //Xử lý trừ tồn kho cho toàn bộ cart
    //Chạy trong 1 transaction (atomic: tất cả thành công hoặc rollback hết)
    @Transactional
    public void processStock(Cart cart) {

        // Duyệt từng item trong giỏ hàng
        for (CartItem cartItem : cart.getCartItems()) {

            // Lấy product mới nhất từ DB (quan trọng để đảm bảo stock current)
            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(() -> new RuntimeException("Not found product"));
            // Nếu không tìm thấy product → ném exception

            int quantity = cartItem.getQuantity();

            if (quantity <= 0) {
                throw new RuntimeException("Invalid quantity");
            }

            // Kiểm tra tồn kho có đủ không
            if (product.getStock() < quantity) {
                // Không đủ hàng → fail toàn bộ transaction
                throw new RuntimeException("Out of stock");
            }

            // Trừ tồn kho
            product.setStock(
                    product.getStock() - quantity
            );

            // Lưu lại DB
            productRepository.save(product);
            // Có @Version → nếu concurrent update → sẽ throw OptimisticLock exception
            //Điều kiện để @Version hoạt động đúng
            //Entity phải được load từ DB (managed entity, tim product tu repository theo id)
            //Version sẽ tự tăng khi entity được update và flush xuống DB
        }
    }
}
