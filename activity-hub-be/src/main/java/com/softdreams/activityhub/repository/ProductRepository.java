package com.softdreams.activityhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.softdreams.activityhub.dto.projection.ProductRatingProjection;
import com.softdreams.activityhub.dto.response.ProductResponse;
import com.softdreams.activityhub.dto.response.lookup.ProductLookupResponse;
import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query("""
            SELECT new com.softdreams.activityhub.dto.response.ProductResponse(
                p.id,
                p.name,
                p.price,
                c.name,
                c.id,
                c.code,
                p.quantity,
                p.description,
                p.image,
                p.createdAt,
                p.updatedAt
            )
            FROM Product p
            LEFT JOIN p.category c
            WHERE (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (
                :categoryId IS NULL
                OR c.id = :categoryId
            )
            """)
    Page<ProductResponse> search(
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            Pageable pageable
    );

    @Query("""
            SELECT new com.softdreams.activityhub.dto.response.ProductResponse(
                p.id,
                p.name,
                p.price,
                c.name,
                c.id,
                c.code,
                p.quantity,
                p.description,
                p.image,
                p.createdAt,
                p.updatedAt
            )
            FROM Product p
            LEFT JOIN p.category c
            WHERE (
                p.id = :productId
            )
            """)
    Optional<ProductResponse> getProductResponseById(
            @Param("productId") String productId
    );

    @Query("""
        SELECT new com.softdreams.activityhub.dto.response.lookup.ProductLookupResponse(
            p.id,
            p.name,
            p.quantity
        )
        FROM Product p
        """)
    Page<ProductLookupResponse> lookup(Pageable pageable);

    boolean existsByCategoryId(String categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") String id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.quantity <= :threshold ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold, Pageable pageable);

}
