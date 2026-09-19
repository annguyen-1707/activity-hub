package com.softdreams.activityhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.softdreams.activityhub.entity.ProductReview;

public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {}
