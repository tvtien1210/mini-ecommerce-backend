package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Cart;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.CartItem;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.UserNotFoundException;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.CreateProductRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Category;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.ProductMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CartItemRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CategoryRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.OrderItemRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, OrderItemRepository orderItemRepository, CartItemRepository cartItemRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductDTO> getAllProduct() {
        return productRepository.findAll().stream().map(product -> ProductMapper.toDTO(product)).toList();
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found product!"));
        return ProductMapper.toDTO(product);
    }


    // Tối ưu hơn
    public List<ProductDTO> saveProduct(List<CreateProductRequest> requests) {

        // 1. Convert danh sách Request (DTO từ client)
        //    → sang danh sách Entity Product để lưu DB
        List<Product> products = requests.stream().map(rq -> {

            // 2. Tìm category theo categoryId từ request
            //    Nếu không tìm thấy → báo lỗi ngay
            Category category = categoryRepository.findById(rq.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Not found category id: " + rq.getCategoryId()));

            // 3. Tạo object Product mới (entity để lưu database)
            Product p = new Product();

            // 4. Map dữ liệu từ request → entity Product
            p.setName(rq.getName());
            p.setDescription(rq.getDescription());
            p.setPrice(rq.getPrice());
            p.setStock(rq.getStock());

            // 5. Gán category (FK relationship)
            //    Nếu không set → category_id trong DB sẽ NULL → lỗi dữ liệu
            p.setCategory(category);

            // 6. Trả về product để đưa vào list
            return p;

        }).toList();

        // 7. Lưu toàn bộ danh sách Product vào database cùng lúc (batch insert)
        List<Product> saved = productRepository.saveAll(products); // 🍺-> saveAll: lưu luôn tất cả một lúc, tối ưu code

        // 8. Convert từ Entity (Product) → DTO để trả về cho client
        // product chính là từng phần tử trong saved (map = for(...))
        return saved.stream()
                .map(product -> ProductMapper.toDTO(product))
                .toList();
    }


    //Không tối ưu, chậm nếu nhiều data (save 1 → save 2 → save 3 ...)
//    public List<ProductDTO> saveProduct(List<CreateProductRequest> requests) {
//
//        // Danh sách kết quả trả về (DTO)
//        List<ProductDTO> result = new ArrayList<>();
//
//        // Duyệt từng request (mỗi request là 1 sản phẩm)
//        for (CreateProductRequest rq : requests) {
//
//            // 1. Tìm category theo id (bắt buộc phải tồn tại)
//            Category category = categoryRepository.findById(rq.getCategoryId())
//                    .orElseThrow(() -> new RuntimeException("Not found category id: " + rq.getCategoryId()));
//
//            // 2. Tạo object Product mới để lưu DB
//            Product product = new Product();
//
//            // 3. Gán dữ liệu từ request sang entity
//            product.setName(rq.getName());
//            product.setDescription(rq.getDescription());
//            product.setPrice(rq.getPrice());
//            product.setStock(rq.getStock());
//
//            // 4. Gán category (QUAN TRỌNG)
//            // Nếu không set -> category_id trong DB sẽ = NULL → lỗi dữ liệu
//            product.setCategory(category);
//
//            // 5. Lưu vào database ‼️save 1 -> save 2 -> save 3 here ...
//            Product savedProduct = productRepository.save(product);
//
//            // 6. Convert từ Entity -> DTO để trả ra API
//            ProductDTO dto = ProductMapper.toDTO(savedProduct);
//
//            // 7. Add vào list kết quả
//            result.add(dto);
//        }
//
//        // Trả về danh sách product đã tạo
//        return result;
//    }

    public ProductDTO updateProduct(Long id, CreateProductRequest rq) {
        Product product = productRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        Category category = categoryRepository.findById(rq.getCategoryId()).orElseThrow(() -> new RuntimeException("Not found category"));

        product.setName(rq.getName());
        product.setDescription(rq.getDescription());
        product.setPrice(rq.getPrice());
        product.setStock(rq.getStock());
        product.setCategory(category);
        return ProductMapper.toDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // check product có đang được sử dụng không
        if (orderItemRepository.existsByProduct(product) || cartItemRepository.existsByProduct(product)) {
            throw new RuntimeException("Cannot delete because product is being used");
        }

        productRepository.delete(product);
    }


}
