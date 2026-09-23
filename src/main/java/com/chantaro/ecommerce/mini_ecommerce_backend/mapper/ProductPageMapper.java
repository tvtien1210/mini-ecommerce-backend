package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductPageDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductPageMapper {

    public static ProductPageDTO toDTO(Page<Product> productPage) {

        // Lấy danh sách Product của trang hiện tại
        // Sau đó convert từng Product Entity → ProductDTO
        List<ProductDTO> products = productPage.getContent()
                .stream()
                .map(ProductMapper::toDTO)
                .toList();

        return new ProductPageDTO(

                        //danh sách ProductDTO của đúng trang hiện tại, không phải toàn bộ Product trong database
                        products,

                        // Lấy số trang hiện tại currentPage
                        // Ví dụ: page = 0 → trang đầu tiên
                        productPage.getNumber(),

                        // Tổng số trang
                        // Ví dụ: 100 Product / 10 Product mỗi trang = 10 trang
                        productPage.getTotalPages(),

                        // Tổng số Product trong Database
                        // Ví dụ: Database có 100 Product → 100
                        productPage.getTotalElements()

        );

    }
}
