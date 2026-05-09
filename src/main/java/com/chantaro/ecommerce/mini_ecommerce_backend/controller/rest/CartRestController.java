package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cart.CartDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.CreateCartItemRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.cartitem.UpdateCartItemRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CartItemRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.CartService;
import jakarta.persistence.PostUpdate;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {
    private CartService cartService;

    @Autowired
    public CartRestController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/my")
    public CartDTO getMyCart(){
        return cartService.getMyCart();
    }

    @PostMapping("/items")
    public CartDTO createCartItem(@Valid @RequestBody CreateCartItemRequest rq){
        return cartService.createCartItem(rq);
    }

    @PutMapping("/items/{id}")
    public CartDTO updateCartItem(@Valid @PathVariable Long id, @RequestBody UpdateCartItemRequest rq){
        return cartService.updateCartItem(id,rq);
    }

    @DeleteMapping("/items/{id}")
    public String deleteCartItem(@PathVariable Long id){
        cartService.deleteCartItem(id);
        return "Deleted item with id = " + id;
    }

}
