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

        //--- PAGINATION
        // Lấy danh sách sản phẩm theo từng trang.
        // Ví dụ: tổng cộng 25 sản phẩm, mỗi trang hiển thị 10 sản phẩm.
        Page<Product> productPage = productService.getProducts(page, size);

        // Lấy danh sách sản phẩm của riêng trang hiện tại.
        // Ví dụ: trang 1 sẽ lấy sản phẩm 1-10,
        // trang 2 lấy sản phẩm 11-20,
        // trang 3 lấy sản phẩm 21-25.
        model.addAttribute("products", productPage.getContent());

        // Lấy số thứ tự của trang hiện tại.
        // Ví dụ: page = 0 là trang đầu tiên, page = 1 là trang thứ hai.
        model.addAttribute("currentPage", page);

        // Lấy tổng số trang cần hiển thị.
        // Ví dụ: 25 sản phẩm / 10 sản phẩm mỗi trang = 2,5
        // => cần 3 trang.
        model.addAttribute("totalPages", productPage.getTotalPages());

        // Lấy tổng số sản phẩm trong database.
        // Ví dụ: database có 25 sản phẩm => totalProducts = 25.
        model.addAttribute("totalProducts", productPage.getTotalElements());

        //---ACTIVE PAGE
        // Gán tên trang hiện tại là "activePage"
        // Trang Products sẽ được đánh dấu là trang đang active
        model.addAttribute("activePage", "products");

        //Lay du lieu roi bind toi trang web products.html
        return "products";
    }

}