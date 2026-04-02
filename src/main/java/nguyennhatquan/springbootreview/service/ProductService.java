package nguyennhatquan.springbootreview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.CreateProductRequest;
import nguyennhatquan.springbootreview.dto.PageResponse;
import nguyennhatquan.springbootreview.dto.ProductResponse;
import nguyennhatquan.springbootreview.dto.UpdateProductRequest;
import nguyennhatquan.springbootreview.entity.Category;
import nguyennhatquan.springbootreview.entity.Product;
import nguyennhatquan.springbootreview.exception.ResourceNotFoundException;
import nguyennhatquan.springbootreview.repository.CategoryRepository;
import nguyennhatquan.springbootreview.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> page = productRepository.findByIsActive(true, pageable);

        log.debug("Fetched {} products from page {}", page.getContent().size(), pageNo);

        return buildPageResponse(page, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getByCategory(Long categoryId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> page = productRepository.findByCategoryIdAndIsActive(categoryId, true, pageable);

        log.debug("Fetched {} products for category {} from page {}", page.getContent().size(), categoryId, pageNo);

        return buildPageResponse(page, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> page = productRepository.findByNameContainingIgnoreCaseAndIsActive(keyword, true, pageable);

        log.debug("Searched {} products with keyword '{}' from page {}", page.getContent().size(), keyword, pageNo);

        return buildPageResponse(page, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .isActive(true)
                .build();

        Product saved = productRepository.save(product);

        log.info("Product created: {} with id: {}", product.getName(), saved.getId());

        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStock(request.getStock());

        Product updated = productRepository.save(existing);

        log.info("Product updated: {} with id: {}", request.getName(), id);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product deleted (soft delete): {}", id);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .isActive(product.getIsActive())
                .build();
    }

    private PageResponse<ProductResponse> buildPageResponse(Page<Product> page, int pageNo, int pageSize) {
        return PageResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).toList())
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
