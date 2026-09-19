package com.softdreams.activityhub.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.StockTransactionLine;

@Repository
public interface StockTransactionLineRepository extends JpaRepository<StockTransactionLine, String> {

    boolean existsByProduct_Id(String productId);

    @Query(
            """
				SELECT l
				FROM StockTransactionLine l
				JOIN FETCH l.product
				WHERE l.stockTransaction.id IN :transactionIds
			""")
    List<StockTransactionLine> findLinesWithProduct(@Param("transactionIds") List<String> transactionIds);
}
