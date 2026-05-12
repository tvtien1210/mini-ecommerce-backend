package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.order.OrderDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.orderstatus.UpdateOrderStatusRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrderRestController {

    private final OrderService orderService;

    @Autowired
    public OrderRestController(OrderService orderService) {
        this.orderService = orderService;
    }

    //Một user có nhiều order -> List<Order> , trong project này dùng Set<Order>  để lưu tập hợp các Order KHÔNG bị trùng lặp
    //getMyOrders() : current User
    @GetMapping("/my")
    public List<OrderDTO> getMyOrders() {
        return orderService.getMyOrders();
    }

//    @PostMapping
//    public OrderDTO createOrder(@Valid @RequestBody CreateOrderRequest rq){
//        return orderService.createOrder(rq);
//    }

    @PostMapping("/checkout")
    public OrderDTO checkoutOrder() {
        return orderService.checkoutOrder();
    }

    @PostMapping("/{id}/pay")
    public void paidOrder(@PathVariable Long orderId){
        orderService.paidOrder(orderId);
    }

    @PatchMapping("/{id}/status")
    public OrderDTO updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest rq) {
        return orderService.updateOrderStatus(id, rq);
    }

    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return "Deleted order by id =" + id;
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public String removeItemFromOrder(@PathVariable Long orderId, @PathVariable Long itemId) {
        orderService.removeItemFromOrder(orderId, itemId);
        return "Item removed from order successfully";
    }


}

/*
GET /orders          admin
GET /orders/my       user (bắt buộc)

PUT /orders/{id}     💀KHÔNG nên, nếu user.id =1 mà đổi sang user.id=2 -> xem trộm đơn người khác
PATCH /orders/{id}/status → chỉ update status*/

