package com.softdreams.activityhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.OrderLine;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, String> {

    boolean existsByProduct_Id(String productId);
}
