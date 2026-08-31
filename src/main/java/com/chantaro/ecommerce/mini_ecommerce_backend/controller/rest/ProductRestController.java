package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.CreateProductRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductRestController {
    private final ProductService productService;

    //GET ALL PRODUCTS
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    //GET PRODUCT BY ID
    @GetMapping("/{productId}")
    public ProductDTO getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }


    //CREATE PRODUCT
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProductDTO> createProduct(@Valid @RequestBody List<CreateProductRequest> rqs) {
        return productService.createProduct(rqs);
    }

    @PutMapping("/{productId}")
    public ProductDTO updateProduct(@PathVariable Long productId, @RequestBody CreateProductRequest rq) {
        return productService.updateProduct(productId, rq);
    }

    @DeleteMapping("/{productId}")
    public String deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return "Deleted product by id = " + productId;
    }

}
