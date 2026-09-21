package com.softdreams.activityhub.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Page<Product> products = productRepository.search(keyword, categoryId, pageable);
        List<String> productIds = products.stream().map(Product::getId).toList();
        List<ProductRatingProjection> ratings = reviewRepository.getProductRatings(productIds);
        Map<String, ProductRatingProjection> ratingMap =
                ratings.stream().collect(Collectors.toMap(ProductRatingProjection::getProductId, Function.identity()));
        return products.map(product -> {
            ProductResponse response = productMapper.toProductResponse(product);

            ProductRatingProjection rating = ratingMap.get(product.getId());

            response.setRate(rating != null ? rating.getAverageRating() : 0.0);
            response.setTotalReviews(rating != null ? rating.getTotalReviews() : 0L);
            return response;
        });
    }

    public ProductResponse getById(String productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        ProductResponse response = productMapper.toProductResponse(product);

        reviewRepository.getProductRating(productId).ifPresent(rating -> {
            if (rating.getAverageRating() != null) {
                response.setRate(rating.getAverageRating());
            }
            if (rating.getTotalReviews() != null) {
                response.setTotalReviews(rating.getTotalReviews());
            }
        });

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
