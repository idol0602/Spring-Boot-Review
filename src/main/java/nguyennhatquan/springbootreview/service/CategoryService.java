package nguyennhatquan.springbootreview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.CategoryResponse;
import nguyennhatquan.springbootreview.dto.CreateCategoryRequest;
import nguyennhatquan.springbootreview.dto.PageResponse;
import nguyennhatquan.springbootreview.dto.UpdateCategoryRequest;
import nguyennhatquan.springbootreview.entity.Category;
import nguyennhatquan.springbootreview.exception.ResourceNotFoundException;
import nguyennhatquan.springbootreview.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getAll(int pageNo, int pageSize) {
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
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return mapToResponse(category);
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

        return mapToResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Category deleted (soft delete): {}", id);
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
