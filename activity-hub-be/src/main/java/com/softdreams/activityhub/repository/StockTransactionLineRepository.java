package com.softdreams.activityhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.StockTransactionLine;

@Repository
public interface StockTransactionLineRepository extends JpaRepository<StockTransactionLine, String> {

    boolean existsByProduct_Id(String productId);
}
