package com.softdreams.activityhub.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.StockTransaction;
import com.softdreams.activityhub.enums.StockTransactionType;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, String> {

    @Query("""
        SELECT st FROM StockTransaction st
        WHERE (:type IS NULL OR st.type = :type)
          AND (:productId IS NULL OR :productId = ''
            OR EXISTS (
                SELECT 1 FROM StockTransactionLine l
                WHERE l.stockTransaction = st AND l.product.id = :productId
            ))
        """)
    Page<StockTransaction> search(
            @Param("type") StockTransactionType type,
            @Param("productId") String productId,
            Pageable pageable);
}
