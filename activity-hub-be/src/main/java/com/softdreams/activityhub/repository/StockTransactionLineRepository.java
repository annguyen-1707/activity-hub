package com.softdreams.activityhub.repository;

import java.util.List;

import com.softdreams.activityhub.dto.response.StockTransactionLineResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.StockTransactionLine;

@Repository
public interface StockTransactionLineRepository extends JpaRepository<StockTransactionLine, String> {

    boolean existsByProduct_Id(String productId);

	@Query("""
        SELECT new com.softdreams.activityhub.dto.response.StockTransactionLineResponse(
            l.id,
            p.id,
            p.name,
            l.quantity,
            l.stockTransaction.id
        )
        FROM StockTransactionLine l
        JOIN l.product p
        WHERE l.stockTransaction.id IN :transactionIds
        """)
	List<StockTransactionLineResponse> findLinesWithProduct(
			@Param("transactionIds") List<String> transactionIds
	);
}