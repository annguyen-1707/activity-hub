package com.softdreams.activityhub.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.Product;
import com.softdreams.activityhub.enums.CategoryEnum;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query(
            """
		SELECT p FROM Product p
		WHERE (:keyword IS NULL OR :keyword = ''
			OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
		AND (:category IS NULL OR p.category = :category)
		""")
    Page<Product> search(@Param("keyword") String keyword, @Param("category") CategoryEnum category, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") String id);
}
