package com.softdreams.activityhub.repository;

import java.util.List;
import java.util.Optional;

import com.softdreams.activityhub.dto.projection.CategoryDistributionProjection;
import com.softdreams.activityhub.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, String id);

    List<Category> findByActiveTrueOrderByCreatedAtDesc();

    @Query(
            value = """
            SELECT new com.softdreams.activityhub.dto.response.CategoryResponse(
                c.id,
                c.code,
                c.name,
                c.description,
                c.active,
                COUNT(p.id),
                c.createdAt,
                c.updatedAt
            )
            FROM Category c
            LEFT JOIN Product p ON p.category.id = c.id
            WHERE (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            GROUP BY
                c.id,
                c.code,
                c.name,
                c.description,
                c.active,
                c.createdAt,
                c.updatedAt
            """,
            countQuery = """
            SELECT COUNT(c.id)
            FROM Category c
            WHERE (
                :keyword IS NULL
                OR :keyword = ''
                OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """
    )
    Page<CategoryResponse> searchWithCount(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT c.name AS categoryName, COUNT(p.id) AS productCount
            FROM Category c
            LEFT JOIN Product p ON p.category.id = c.id
            GROUP BY c.id, c.name
            """)
    List<CategoryDistributionProjection> getCategoryDistribution();
}
