package com.softdreams.activityhub.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.softdreams.activityhub.dto.response.lookup.ProductLookupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.projection.ProductRatingProjection;
import com.softdreams.activityhub.dto.request.ProductRequest;
import com.softdreams.activityhub.dto.response.ProductResponse;
import com.softdreams.activityhub.entity.Category;
import com.softdreams.activityhub.entity.Product;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.ProductMapper;
import com.softdreams.activityhub.repository.*;

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
    CategoryRepository categoryRepository;
    OrderLineRepository orderLineRepository;
    StockTransactionLineRepository stockTransactionLineRepository;
    ProductMapper productMapper;
    ReviewRepository reviewRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        Product product = productMapper.toProduct(request);
        product.setCategory(category);
        return productMapper.toProductResponse(productRepository.save(product));
    }

    public Page<ProductResponse> search(String keyword, String categoryId, Pageable pageable) {
        Page<ProductResponse> products = productRepository.search(keyword, categoryId, pageable);
//        List<String> productIds = products.stream().map(ProductResponse::getId).toList();
//        List<ProductRatingProjection> ratings = reviewRepository.getProductRatings(productIds);
//        Map<String, ProductRatingProjection> ratingMap =
//                ratings.stream().collect(Collectors.toMap(ProductRatingProjection::getProductId, Function.identity()));
//        return products.map(product -> {
//            ProductRatingProjection rating = ratingMap.get(product.getId());
//
//            double avg = rating != null && rating.getAverageRating() != null
//                    ? Math.round(rating.getAverageRating() * 10.0) / 10.0
//                    : 0.0;
//            product.setRate(avg);
//            product.setTotalReviews(rating != null && rating.getTotalReviews() != null ? rating.getTotalReviews() : 0L);
//            return product;
//        });
        return products;
    }

    public Page<ProductLookupResponse> lookup(Pageable pageable) {
        return productRepository.lookup(pageable);
    }

    public ProductResponse getById(String productId) {
        ProductResponse response = productRepository
                .getProductResponseById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse update(String productId, ProductRequest request) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            product.setCategory(category);
        }

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
