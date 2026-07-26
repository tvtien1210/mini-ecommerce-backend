package com.chantaro.ecommerce.mini_ecommerce_backend.controller.rest;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.CreateProductRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {
    private final ProductService productService;

    @Autowired
    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductDTO> getAll() {
        return productService.getAllProduct();
    }

    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public List<ProductDTO> create(@RequestBody List<CreateProductRequest> rqs) {
        return productService.saveProduct(rqs);
    }

    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @RequestBody CreateProductRequest rq) {
        return productService.updateProduct(id, rq);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "Deleted product by id = " + id;
    }
}
