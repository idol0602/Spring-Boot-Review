package nguyennhatquan.springbootreview.product.service;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.product.dto.category.CategoryResponse;
import nguyennhatquan.springbootreview.product.dto.category.CreateCategoryRequest;
import nguyennhatquan.springbootreview.shared.dto.PageResponse;
import nguyennhatquan.springbootreview.product.dto.category.UpdateCategoryRequest;
import nguyennhatquan.springbootreview.product.entity.Category;
import nguyennhatquan.springbootreview.shared.exception.ResourceNotFoundException;
import nguyennhatquan.springbootreview.product.repository.CategoryRepository;
import nguyennhatquan.springbootreview.shared.service.cache.MultiLevelCacheService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MultiLevelCacheService cacheService;

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAll(int pageNo, int pageSize) {
        String key = "category_all_page_" + pageNo + "_size_" + pageSize;
        return cacheService.get(key, () -> {
            Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNo, pageSize);
            Page<Category> page = categoryRepository.findByIsActive(true, pageable);
            log.debug("Fetched {} categories from page {}", page.getContent().size(), pageNo);

            return PageResponse.<CategoryResponse>builder()
                    .content(page.getContent().stream().map(this::mapToResponse).toList())
                    .pageNo(pageNo)
                    .pageSize(pageSize)
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .isFirst(page.isFirst())
                    .isLast(page.isLast())
                    .build();
        }, new TypeReference<PageResponse<CategoryResponse>>() {});
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        String key = "category_" + id;
        return cacheService.get(key, () -> {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
            return mapToResponse(category);
        }, new TypeReference<CategoryResponse>() {});
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(true)
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Category created: {} with id: {}", request.getName(), saved.getId());

        evictCategoryCaches(null);

        return mapToResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        Category updated = categoryRepository.save(existing);
        log.info("Category updated: {} with id: {}", request.getName(), id);

        evictCategoryCaches(id);

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category deleted (soft delete): {}", id);

        evictCategoryCaches(id);
    }

    private void evictCategoryCaches(Long id) {
        if (id != null) {
            cacheService.evict("category", String.valueOf(id));
        }
        cacheService.evictPrefix("category_all_page_");
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(category.getIsActive())
                .build();
    }
}
