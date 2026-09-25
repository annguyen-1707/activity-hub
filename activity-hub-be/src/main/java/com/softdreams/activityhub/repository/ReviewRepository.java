package com.softdreams.activityhub.repository;

import java.util.List;
import java.util.Optional;

import com.softdreams.activityhub.dto.projection.TopRatedProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.dto.projection.ProductRatingProjection;
import com.softdreams.activityhub.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    Optional<Review> findByOrderLine_Id(String orderLineId);

    boolean existsByOrderLine_Id(String orderLineId);

    List<Review> findByOrderLine_IdIn(List<String> orderLineIds);

    Page<Review> findByOrderLine_Product_Id(String productId, Pageable pageable);

}
