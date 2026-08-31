package com.chantaro.ecommerce.mini_ecommerce_backend.service;

import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.CreateProductRequest;
import com.chantaro.ecommerce.mini_ecommerce_backend.dto.product.ProductDTO;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Category;
import com.chantaro.ecommerce.mini_ecommerce_backend.entity.Product;
import com.chantaro.ecommerce.mini_ecommerce_backend.enums.ErrorCode;
import com.chantaro.ecommerce.mini_ecommerce_backend.exception.BusinessException;
import com.chantaro.ecommerce.mini_ecommerce_backend.mapper.ProductMapper;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CartItemRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.CategoryRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.OrderItemRepository;
import com.chantaro.ecommerce.mini_ecommerce_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final CategoryRepository categoryRepository;

    //GET ALL PRODUCT
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(product -> ProductMapper.toDTO(product)).toList();
    }


    //GET PRODUCT BY ID
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductMapper.toDTO(product);
    }



    //CREATE PRODUCT

    // Tối ưu hơn
    // 最適化された方法（パフォーマンス向上）
    @Transactional
    public List<ProductDTO> createProduct(List<CreateProductRequest> requests) {

        // Convert danh sách Request (DTO từ client) → sang danh sách Entity Product để lưu DB
        // クライアントから受け取った Request DTO のリストをDB保存用の Product Entity リストへ変換する
        List<Product> products = requests.stream().map(rq -> {

            // Tìm category theo categoryId từ request -> Nếu không tìm thấy → báo lỗi ngay
            // request の categoryId を使って Category を検索 -> 見つからない場合は即エラーを返す
            Category category = categoryRepository.findById(rq.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

            //Tạo object Product mới (entity để lưu database)
            //DB保存用の新しい Product Entity を作成
            Product p = new Product();

            //Map dữ liệu từ request → entity Product
            //request のデータを Product Entity にマッピング
            p.setName(rq.getName());
            p.setDescription(rq.getDescription());
            p.setPrice(rq.getPrice());
            p.setCurrency(rq.getCurrency());
            p.setStock(rq.getStock());
            p.setImageUrl(rq.getImageUrl());

            // Gán category (FK relationship) -> Nếu không set → category_id trong DB sẽ NULL → lỗi dữ liệu
            //category を設定（外部キー関連）-> 設定しない場合、DB の category_id が NULL になりデータ不整合が発生する
            p.setCategory(category);

            // Trả về product để đưa vào list
            // list に追加するため Product を返す
            return p;

        }).toList();

        //Lưu toàn bộ danh sách Product vào database cùng lúc (batch insert)
        //Product リストを一括で DB に保存する（バッチインサート）
        List<Product> saved = productRepository.saveAll(products); // 🍺-> saveAll: lưu luôn tất cả một lúc, tối ưu code
        // 🍺 saveAll: 一括保存することでコードとパフォーマンスを最適化

        // Convert từ Entity (Product) → DTO để trả về cho client
        // product chính là từng phần tử trong saved (map = for(...))
        // Entity(Product) → DTO に変換してクライアントへ返却
        // product は saved リスト内の各要素
        return saved.stream()
                .map(product -> ProductMapper.toDTO(product))
                .toList();
    }


    //Không tối ưu, chậm nếu nhiều data (save 1 → save 2 → save 3 ...)
    // 最適化されていない方法。データ量が多い場合は遅くなる（1件ずつ保存）
//    public List<ProductDTO> saveProduct(List<CreateProductRequest> requests) {
//
//        // Danh sách kết quả trả về (DTO)
//        // 返却用DTOリスト
//        List<ProductDTO> result = new ArrayList<>();
//
//        // Duyệt từng request (mỗi request là 1 sản phẩm)
//        // request を1件ずつ処理（1 request = 1商品）
//        for (CreateProductRequest rq : requests) {
//
//            // 1. Tìm category theo id (bắt buộc phải tồn tại)
//            // 1. id を使って Category を検索（必須存在）
//            Category category = categoryRepository.findById(rq.getCategoryId())
//                    .orElseThrow(() -> new RuntimeException("Not found category id: " + rq.getCategoryId()));
//
//            // 2. Tạo object Product mới để lưu DB
//            // 2. DB保存用 Product Entity を作成
//            Product product = new Product();
//
//            // 3. Gán dữ liệu từ request sang entity
//            // 3. request データを entity にセット
//            product.setName(rq.getName());
//            product.setDescription(rq.getDescription());
//            product.setPrice(rq.getPrice());
//            product.setStock(rq.getStock());
//
//            // 4. Gán category (QUAN TRỌNG)
//            // Nếu không set -> category_id trong DB sẽ = NULL → lỗi dữ liệu
//            // 4. category を設定（重要）
//            // 設定しない場合、DB の category_id が NULL になり不整合が発生
//            product.setCategory(category);
//
//            // 5. Lưu vào database ‼️save 1 -> save 2 -> save 3 here ...
//            // 5. DB に1件ずつ保存
//            Product savedProduct = productRepository.save(product);
//
//            // 6. Convert từ Entity -> DTO để trả ra API
//            // 6. Entity → DTO に変換して API レスポンスへ
//            ProductDTO dto = ProductMapper.toDTO(savedProduct);
//
//            // 7. Add vào list kết quả
//            // 7. 結果リストへ追加
//            result.add(dto);
//        }
//
//        // Trả về danh sách product đã tạo
//        // 作成した Product リストを返却
//        return result;
//    }

    public ProductDTO updateProduct(Long id, CreateProductRequest rq) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category category = categoryRepository.findById(rq.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        product.setName(rq.getName());
        product.setDescription(rq.getDescription());
        product.setPrice(rq.getPrice());
        product.setStock(rq.getStock());
        product.setCategory(category);

        return ProductMapper.toDTO(productRepository.save(product));
    }

    public void deleteProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // check product có đang được sử dụng không
        // product が現在使用中かチェックする
        if (orderItemRepository.existsByProduct(product) || cartItemRepository.existsByProduct(product)) {
            throw new BusinessException(ErrorCode.PRODUCT_IN_USE);
        }

        productRepository.delete(product);
    }

    //PAGINATION

    public Page<Product> getProducts(int page, int size) {

        // Tạo yêu cầu phân trang: page = trang, size = số sản phẩm/trang
        Pageable pageable = PageRequest.of(page, size);

        // Lấy danh sách Product theo yêu cầu phân trang từ database
        return productRepository.findAll(pageable);
    }

}