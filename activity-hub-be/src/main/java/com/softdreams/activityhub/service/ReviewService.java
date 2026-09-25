package com.softdreams.activityhub.service;

import java.time.LocalDateTime;

import com.softdreams.activityhub.entity.*;
import com.softdreams.activityhub.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.ReviewRequest;
import com.softdreams.activityhub.dto.response.ReviewResponse;
import com.softdreams.activityhub.enums.OrderStatus;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.ReviewMapper;
import com.softdreams.activityhub.repository.OrderLineRepository;
import com.softdreams.activityhub.repository.ReviewRepository;
import com.softdreams.activityhub.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    static final long REVIEW_WINDOW_DAYS = 30;

    ReviewRepository reviewRepository;
    OrderLineRepository orderLineRepository;
    UserRepository userRepository;
    ReviewMapper reviewMapper;
    ProductRepository productRepository;

    @Transactional
    public ReviewResponse create(String orderLineId, ReviewRequest request) {
        OrderLine orderLine = orderLineRepository
                .findById(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_LINE_NOT_EXISTED));

        User user = getMyUser();
        validateReviewable(orderLine, user);

        if (reviewRepository.existsByOrderLine_Id(orderLineId)) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .orderLine(orderLine)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        ReviewResponse response = reviewMapper.toResponse(reviewRepository.save(review));
        Product product = orderLine.getProduct();
        productRepository.updateProductRating(product.getId());
        return response;
    }

    @Transactional
    public ReviewResponse update(String orderLineId, ReviewRequest request) {
        OrderLine orderLine = orderLineRepository
                .findById(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_LINE_NOT_EXISTED));

        validateReviewable(orderLine, getMyUser());

        Review review = reviewRepository
                .findByOrderLine_Id(orderLineId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_EXISTED));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        ReviewResponse response = reviewMapper.toResponse(reviewRepository.save(review));

        Product product = orderLine.getProduct();
        productRepository.updateProductRating(product.getId());

        return response;
    }

    public ReviewResponse getByOrderLine(String orderLineId) {
        return reviewRepository
                .findByOrderLine_Id(orderLineId)
                .map(reviewMapper::toResponse)
                .orElse(null);
    }

    public Page<ReviewResponse> getByProduct(String productId, Pageable pageable) {
        return reviewRepository.findByOrderLine_Product_Id(productId, pageable).map(reviewMapper::toResponse);
    }

    /**
     * Only the order's own buyer may review it, and only once it has been delivered within the last 30 days.
     */
    private void validateReviewable(OrderLine orderLine, User user) {
        Order order = orderLine.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_NOT_COMPLETED);
        }

        LocalDateTime deliveredAt = order.getCompletedAt();
        if (deliveredAt == null || deliveredAt.plusDays(REVIEW_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REVIEW_WINDOW_EXPIRED);
        }
    }

    private User getMyUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
