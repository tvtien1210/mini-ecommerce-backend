package com.chantaro.ecommerce.mini_ecommerce_backend.mapper;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;

public class ProductMapper {
    public static ProductDTO toDTO(Product product){
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStock(),
                product.getImageUrl(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

/*
// Code cho nguoi moi hoc
@Component
public class ProductMapper {

    public ProductDTO toDTO(Product product) {

        ProductDTO productDTO = new ProductDTO();

        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(product.getPrice());
        productDTO.setCurrency(product.getCurrency());
        productDTO.setStock(product.getStock());
        productDTO.setImageUrl(product.getImageUrl());

        productDTO.setCategoryId(product.getCategory().getId());
        productDTO.setCategoryName(product.getCategory().getName());

        productDTO.setCreatedAt(product.getCreatedAt());
        productDTO.setUpdatedAt(product.getUpdatedAt());

        return productDTO;
    }
}

//@Autowired trong Product Service
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    // ...
}
*/
