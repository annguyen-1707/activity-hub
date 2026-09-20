package com.softdreams.activityhub.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.CategoryRequest;
import com.softdreams.activityhub.dto.response.CategoryResponse;
import com.softdreams.activityhub.entity.Category;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.CategoryMapper;
import com.softdreams.activityhub.repository.CategoryRepository;
import com.softdreams.activityhub.repository.ProductRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {

    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse create(CategoryRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (categoryRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        Category category = categoryMapper.toCategory(request);
        category.setCode(code);
        Category saved = categoryRepository.save(category);
        return toResponseWithCount(saved);
    }

    public List<CategoryResponse> getAllActive() {
        return categoryRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponseWithCount)
                .toList();
    }

    public Page<CategoryResponse> search(String keyword, Pageable pageable) {
        return categoryRepository.search(keyword, pageable).map(this::toResponseWithCount);
    }

    public CategoryResponse getById(String id) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        return toResponseWithCount(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse update(String id, CategoryRequest request) {
        Category category =
                categoryRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        String code = request.getCode().trim().toUpperCase();
        if (categoryRepository.existsByCodeAndIdNot(code, id)) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }

        categoryMapper.updateCategory(category, request);
        category.setCode(code);
        Category saved = categoryRepository.save(category);
        return toResponseWithCount(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }

        if (productRepository.existsByCategoryId(id)) {
            throw new AppException(ErrorCode.CATEGORY_IN_USE);
        }

        categoryRepository.deleteById(id);
    }

    private CategoryResponse toResponseWithCount(Category category) {
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        response.setProductCount(productRepository.countByCategoryId(category.getId()));
        return response;
    }
}
