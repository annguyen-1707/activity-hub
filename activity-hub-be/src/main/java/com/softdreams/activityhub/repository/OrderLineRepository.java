package com.softdreams.activityhub.repository;

import com.softdreams.activityhub.dto.projection.OrderLineProjection;
import com.softdreams.activityhub.dto.response.OrderLineResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.OrderLine;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, String> {

    boolean existsByProduct_Id(String productId);

    @Query(value = """
    SELECT
        ol.id AS id,
        ol.order_id AS orderId,
        p.id AS productId,
        p.name AS productName,
        ol.quantity AS quantity,
        ol.unit_price AS unitPrice,
        ol.subtotal AS subtotal
    FROM order_lines ol
    INNER JOIN products p ON p.id = ol.product_id
    WHERE ol.order_id IN (:orderIds)
    """, nativeQuery = true)
    List<OrderLineProjection> getOrderLinesByOrderIds(
            @Param("orderIds") List<String> orderIds
    );
}
