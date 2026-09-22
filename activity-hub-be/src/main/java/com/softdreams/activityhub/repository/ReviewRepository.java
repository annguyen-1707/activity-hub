package com.softdreams.activityhub.repository;

import java.util.List;
import java.util.Optional;

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

    @Query(
            """
                    SELECT
                        r.orderLine.product.id AS productId,
                        AVG(r.rating) AS averageRating,
                        COUNT(r) AS totalReviews
                    FROM Review r
                    WHERE r.orderLine.product.id = :productId
                    GROUP BY r.orderLine.product.id
                    """)
    Optional<ProductRatingProjection> getProductRating(@Param("productId") String productId);

    @Query(value = """
            SELECT
                p.id AS productId,
                COALESCE(AVG(r.rating), 0) AS averageRating,
                COUNT(r.id) AS totalReviews
            FROM products p
            LEFT JOIN order_lines ol
                ON ol.product_id = p.id
            LEFT JOIN reviews r
                ON r.order_line_id = ol.id
            WHERE p.id IN (:productIds)
            GROUP BY p.id
            """, nativeQuery = true)
    List<ProductRatingProjection> getProductRatings(
            @Param("productIds") List<String> productIds
    );

    @Query("""
        SELECT
            p.id AS productId,
            p.name AS productName,
            p.image AS productImage,
            p.price AS price,
            c.name AS categoryName,
            AVG(r.rating) AS averageRating,
            COUNT(r.id) AS totalReviews
        FROM Review r
        JOIN r.orderLine ol
        JOIN ol.product p
        LEFT JOIN p.category c
        GROUP BY p.id, p.name, p.image, p.price, c.name
        HAVING COUNT(r.id) > 0
        ORDER BY AVG(r.rating) DESC, COUNT(r.id) DESC
    """)
    List<com.softdreams.activityhub.dto.projection.TopRatedProductProjection> getTopRatedProducts(
            Pageable pageable);
}
