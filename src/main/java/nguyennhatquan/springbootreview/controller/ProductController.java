package nguyennhatquan.springbootreview.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.*;
import nguyennhatquan.springbootreview.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAll(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Get all products - page: {}, size: {}", pageNo, pageSize);

        PageResponse<ProductResponse> data = productService.getAll(pageNo, pageSize);

        ApiResponse<PageResponse<ProductResponse>> response = ApiResponse.<PageResponse<ProductResponse>>builder()
                .code(200)
                .message("Products retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Get products by category {} - page: {}, size: {}", categoryId, pageNo, pageSize);

        PageResponse<ProductResponse> data = productService.getByCategory(categoryId, pageNo, pageSize);

        ApiResponse<PageResponse<ProductResponse>> response = ApiResponse.<PageResponse<ProductResponse>>builder()
                .code(200)
                .message("Products retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("Search products with keyword: {} - page: {}, size: {}", keyword, pageNo, pageSize);

        PageResponse<ProductResponse> data = productService.search(keyword, pageNo, pageSize);

        ApiResponse<PageResponse<ProductResponse>> response = ApiResponse.<PageResponse<ProductResponse>>builder()
                .code(200)
                .message("Products retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        log.info("Get product by id: {}", id);

        ProductResponse data = productService.getById(id);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .code(200)
                .message("Product retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        log.info("Create product: {}", request.getName());

        ProductResponse data = productService.create(request);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .code(201)
                .message("Product created successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("Update product with id: {}", id);

        ProductResponse data = productService.update(id, request);

        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .code(200)
                .message("Product updated successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        log.info("Delete product with id: {}", id);

        productService.delete(id);

        ApiResponse<Object> response = ApiResponse.builder()
                .code(200)
                .message("Product deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
