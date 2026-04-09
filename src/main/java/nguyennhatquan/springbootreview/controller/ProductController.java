package nguyennhatquan.springbootreview.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.Product.CreateProductRequest;
import nguyennhatquan.springbootreview.dto.Product.ProductResponse;
import nguyennhatquan.springbootreview.dto.Product.UpdateProductRequest;
import nguyennhatquan.springbootreview.dto.common.ApiResponse;
import nguyennhatquan.springbootreview.dto.common.PageResponse;
import nguyennhatquan.springbootreview.exception.InvalidPaginationParameterException;
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
        try {
            log.info("Get all products - page: {}, size: {}", pageNo, pageSize);

            if (pageNo < 0 || pageSize <= 0) {
                log.warn("Invalid pagination parameters - pageNo: {}, pageSize: {}", pageNo, pageSize);
                throw new InvalidPaginationParameterException(
                        "Invalid pagination parameters: pageNo must be >= 0 and pageSize must be > 0");
            }

            PageResponse<ProductResponse> data = productService.getAll(pageNo, pageSize);

            ApiResponse<PageResponse<ProductResponse>> response = ApiResponse.<PageResponse<ProductResponse>>builder()
                    .code(200)
                    .message("Products retrieved successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving all products", e);
            throw e;
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("Get products by category {} - page: {}, size: {}", categoryId, pageNo, pageSize);

            if (categoryId == null || categoryId <= 0) {
                log.warn("Invalid category ID: {}", categoryId);
                throw new InvalidPaginationParameterException("Invalid category ID: must be a positive number");
            }

            if (pageNo < 0 || pageSize <= 0) {
                log.warn("Invalid pagination parameters - pageNo: {}, pageSize: {}", pageNo, pageSize);
                throw new InvalidPaginationParameterException(
                        "Invalid pagination parameters: pageNo must be >= 0 and pageSize must be > 0");
            }

            PageResponse<ProductResponse> data = productService.getByCategory(categoryId, pageNo, pageSize);

            ApiResponse<PageResponse<ProductResponse>> response = ApiResponse.<PageResponse<ProductResponse>>builder()
                    .code(200)
                    .message("Products retrieved successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving products by category: {}", categoryId, e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("Search products with keyword: {} - page: {}, size: {}", keyword, pageNo, pageSize);

            if (keyword == null || keyword.trim().isEmpty()) {
                log.warn("Invalid search keyword");
                throw new InvalidPaginationParameterException("Search keyword cannot be empty");
            }

            if (pageNo < 0 || pageSize <= 0) {
                log.warn("Invalid pagination parameters - pageNo: {}, pageSize: {}", pageNo, pageSize);
                throw new InvalidPaginationParameterException(
                        "Invalid pagination parameters: pageNo must be >= 0 and pageSize must be > 0");
            }

            PageResponse<ProductResponse> data = productService.search(keyword, pageNo, pageSize);

            ApiResponse<PageResponse<ProductResponse>> response = ApiResponse.<PageResponse<ProductResponse>>builder()
                    .code(200)
                    .message("Products retrieved successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching products with keyword: {}", keyword, e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        try {
            log.info("Get product by id: {}", id);

            if (id == null || id <= 0) {
                log.warn("Invalid product ID: {}", id);
                throw new InvalidPaginationParameterException("Invalid product ID: must be a positive number");
            }

            ProductResponse data = productService.getById(id);

            ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                    .code(200)
                    .message("Product retrieved successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving product by id: {}", id, e);
            throw e;
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        try {
            log.info("Create product: {}", request.getName());

            if (request == null) {
                log.warn("Product creation request is null");
                ApiResponse<ProductResponse> errorResponse = ApiResponse.<ProductResponse>builder()
                        .code(400)
                        .message("Product creation request cannot be null")
                        .timestamp(LocalDateTime.now())
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ProductResponse data = productService.create(request);

            ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                    .code(201)
                    .message("Product created successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating product: {}", request != null ? request.getName() : "unknown", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        try {
            log.info("Update product with id: {}", id);

            if (id == null || id <= 0) {
                log.warn("Invalid product ID for update: {}", id);
                throw new InvalidPaginationParameterException("Invalid product ID: must be a positive number");
            }

            if (request == null) {
                log.warn("Product update request is null");
                ApiResponse<ProductResponse> errorResponse = ApiResponse.<ProductResponse>builder()
                        .code(400)
                        .message("Product update request cannot be null")
                        .timestamp(LocalDateTime.now())
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ProductResponse data = productService.update(id, request);

            ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                    .code(200)
                    .message("Product updated successfully")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating product with id: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        try {
            log.info("Delete product with id: {}", id);

            if (id == null || id <= 0) {
                log.warn("Invalid product ID for deletion: {}", id);
                throw new InvalidPaginationParameterException("Invalid product ID: must be a positive number");
            }

            productService.delete(id);

            ApiResponse<Object> response = ApiResponse.builder()
                    .code(200)
                    .message("Product deleted successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting product with id: {}", id, e);
            throw e;
        }
    }
}
