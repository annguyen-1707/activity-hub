package com.softdreams.activityhub.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.softdreams.activityhub.dto.projection.ProductRatingProjection;
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

    @Query(
            """
                    SELECT p FROM Product p
                    LEFT JOIN FETCH p.category c
                    WHERE (:keyword IS NULL OR :keyword = ''
                        OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
                    AND (:categoryId IS NULL
                         OR c.id = :categoryId)
                    """)
    Page<Product> search(@Param("keyword") String keyword, @Param("categoryId") String categoryId, Pageable pageable);

    boolean existsByCategoryId(String categoryId);

    long countByCategoryId(String categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") String id);

}
