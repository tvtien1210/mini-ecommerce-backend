package com.chantaro.ecommerce.mini_ecommerce_backend.controller.page;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductViewController {
    private final ProductService productService;


    @GetMapping("/products")
    public String showProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        //---PRODUCT AUTO BIND BASED ON DATABASE
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);


        //---PAGINATION
        //lay tat ca san pham va chi thanh trang, moi trang co size = 10 san pham
        Page<Product> productPage = productService.getProducts(page, size);

        //lay rieng danh sach cua trang hien tai, vi du lay 25 san pham size = 10
        model.addAttribute("products", productPage.getContent());

        //lay rieng trang hien tai la trang 1 hay trang 2
        model.addAttribute("currentPage", page);

        //lay tong so trang can hien thi, vi du 25 may /10 may moi trang = 2,5 (la 3 trang)
        model.addAttribute("totalPages", productPage.getTotalPages());

        //lay du lieu tong so may hien thi ra ngoai man hinh
        model.addAttribute("totalProducts", productPage.getTotalElements());

        //---ACTIVE PAGE
        // Gán tên trang hiện tại là "activePage"
        // Trang Products sẽ được đánh dấu là trang đang active
        model.addAttribute("activePage", "products");

        //Lay du lieu roi bind toi trang web products.html
        return "products";
    }

}