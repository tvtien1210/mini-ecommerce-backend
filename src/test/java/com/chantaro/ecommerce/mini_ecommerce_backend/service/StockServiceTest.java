package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Order;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.OrderItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


// StockService Unit Test
// GIVEN → WHEN → THEN
public class StockServiceTest {

    // Service thật cần test
    private StockService stockService;

    @BeforeEach
    void setUp() {
        // Tạo StockService mới trước mỗi test
        stockService = new StockService();
    }


    // TEST 1: Happy Path
    // Reserve stock thành công

    @Test
    void reserveStock_shouldIncreaseReservedStock() {

        // GIVEN
        Product product = new Product();

        // Stock thật = 10, đang có 2 sản phẩm được reserve
        product.setStock(10);
        product.setReservedStock(2);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(3);
        orderItem.setPrice(new BigDecimal("100000"));

        Order order = new Order();
        order.addItem(orderItem);

        // WHEN
        stockService.reserveStock(order);

        // THEN
        // reservedStock: 2 + 3 = 5
        assertEquals(5, product.getReservedStock());

        // Stock thật chưa bị trừ
        assertEquals(10, product.getStock());
    }


    // TEST 2: Out of Stock

    @Test
    void reserveStock_shouldThrowException_whenOutOfStock() {

        // GIVEN
        Product product = new Product();

        // Available = 10 - 2 = 8
        product.setStock(10);
        product.setReservedStock(2);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(9);
        orderItem.setPrice(new BigDecimal("100000"));

        Order order = new Order();
        order.addItem(orderItem);

        // WHEN
        BusinessException businessException =
                assertThrows(
                        BusinessException.class,
                        () -> stockService.reserveStock(order)
                );

        // THEN
        assertEquals(
                "Product out of stock",
                businessException.getMessage()
        );
    }


    // TEST 3: Invalid Quantity

    @Test
    void reserveStock_shouldThrowException_whenQuantityIsInvalid() {

        // GIVEN
        Product product = new Product();

        product.setStock(10);
        product.setReservedStock(2);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(0);
        orderItem.setPrice(new BigDecimal("0"));

        Order order = new Order();
        order.addItem(orderItem);

        // WHEN
        BusinessException businessException =
                assertThrows(
                        BusinessException.class,
                        () -> stockService.reserveStock(order)
                );

        // THEN
        assertEquals(
                "Invalid quantity",
                businessException.getMessage()
        );
    }


    // TEST 4: Confirm Reserved Stock
    // Payment thành công

    @Test
    void confirmReservedStock_shouldDecreaseStockAndReservedStock() {

        // GIVEN
        Product product = new Product();

        // Stock thật = 10
        product.setStock(10);

        // Tổng reservedStock hiện tại = 5
        product.setReservedStock(5);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(3);
        orderItem.setPrice(new BigDecimal("100000"));

        Order order = new Order();
        order.addItem(orderItem);

        // WHEN
        // lúc này code đi qua confirmReservedStock(order) luôn rồi nên ReservedStock là tổng giữ kho của tất cả luôn,
        // bao gồm của user hiện tại luôn
        stockService.confirmReservedStock(order);

        // THEN
        // Stock thật: 10 - 3 = 7
        assertEquals(7, product.getStock());

        // ReservedStock: 5 - 3 = 2
        assertEquals(2, product.getReservedStock());
    }


    // TEST 5: Release Reserved Stock
    // Payment thất bại / khách không mua

    @Test
    void releaseReservedStock_shouldDecreaseReservedStock() {

        // GIVEN
        Product product = new Product();

        // Stock thật không bị trừ
        product.setStock(10);

        // Tổng reservedStock hiện tại = 5
        product.setReservedStock(5);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(3);
        orderItem.setPrice(new BigDecimal("100000"));

        Order order = new Order();
        order.addItem(orderItem);

        // WHEN
        // lúc này code đi qua releaseReservedStock(order) luôn rồi nên ReservedStock là tổng giữ kho của tất cả luôn,
        // bao gồm của user hiện tại luôn
        stockService.releaseReservedStock(order);

        // THEN
        // Stock thật không thay đổi
        assertEquals(10, product.getStock());

        // ReservedStock: 5 - 3 = 2
        assertEquals(2, product.getReservedStock());
    }
}