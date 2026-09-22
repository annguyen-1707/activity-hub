package com.softdreams.activityhub.repository;

import java.time.LocalDateTime;
import java.util.List;

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

    @Query(
            """
				SELECT st
				FROM StockTransaction st
				JOIN FETCH st.createdBy
				WHERE (:type IS NULL OR st.type = :type)
				AND (CAST(:fromDate AS timestamp) IS NULL OR st.createdAt >= :fromDate)
				AND (CAST(:toDate AS timestamp) IS NULL OR st.createdAt <= :toDate)
				AND (
					:productId IS NULL
					OR :productId = ''
					OR EXISTS (
						SELECT 1
						FROM StockTransactionLine l
						WHERE l.stockTransaction = st
						AND l.product.id = :productId
					)
				)
			""")
    Page<StockTransaction> search(
            @Param("type") StockTransactionType type,
            @Param("productId") String productId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(
            """
				SELECT st
				FROM StockTransaction st
				JOIN FETCH st.createdBy
				WHERE (:type IS NULL OR st.type = :type)
				AND (CAST(:fromDate AS timestamp) IS NULL OR st.createdAt >= :fromDate)
				AND (CAST(:toDate AS timestamp) IS NULL OR st.createdAt <= :toDate)
				AND (
					:productId IS NULL
					OR :productId = ''
					OR EXISTS (
						SELECT 1
						FROM StockTransactionLine l
						WHERE l.stockTransaction = st
						AND l.product.id = :productId
					)
				)
				ORDER BY st.createdAt DESC
			""")
    List<StockTransaction> findForExport(
            @Param("type") StockTransactionType type,
            @Param("productId") String productId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);
}
