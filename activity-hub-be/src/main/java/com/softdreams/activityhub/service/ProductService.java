package com.softdreams.activityhub.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.ProductRequest;
import com.softdreams.activityhub.dto.response.ProductResponse;
import com.softdreams.activityhub.entity.Product;
import com.softdreams.activityhub.enums.CategoryEnum;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.ProductMapper;
import com.softdreams.activityhub.repository.OrderLineRepository;
import com.softdreams.activityhub.repository.ProductRepository;
import com.softdreams.activityhub.repository.StockTransactionLineRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    OrderLineRepository orderLineRepository;
    StockTransactionLineRepository stockTransactionLineRepository;
    ProductMapper productMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toProduct(request);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public Page<ProductResponse> search(String keyword, CategoryEnum category, Pageable pageable) {
        return productRepository.search(keyword, category, pageable).map(productMapper::toProductResponse);
    }

    public ProductResponse getById(String productId) {
        return productMapper.toProductResponse(productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(String productId, ProductRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        productMapper.updateProduct(product, request);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        if (orderLineRepository.existsByProduct_Id(productId)
                || stockTransactionLineRepository.existsByProduct_Id(productId)) {
            throw new AppException(ErrorCode.PRODUCT_IN_USE);
        }

        productRepository.deleteById(productId);
    }
}
