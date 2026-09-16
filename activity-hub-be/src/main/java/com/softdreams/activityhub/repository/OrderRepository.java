package com.softdreams.activityhub.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderLines WHERE o.id = :orderId")
    Optional<Order> findByIdWithLines(@Param("orderId") String orderId);

    boolean existsByIdAndUserId(String orderId, String userId);

    @Query(
            value = """
        SELECT o.*
        FROM orders o
        INNER JOIN users u ON u.id = o.user_id
        WHERE o.user_id = :userId
        AND (
            :keyword IS NULL
            OR u.username LIKE CONCAT('%', :keyword, '%')
            OR u.first_name LIKE CONCAT('%', :keyword, '%')
            OR u.last_name LIKE CONCAT('%', :keyword, '%')
        )
        AND (
            :status IS NULL
            OR o.status = :status
        )
        AND (
            :paymentMethod IS NULL
            OR o.payment_method = :paymentMethod
        )
        AND (
            :fromDate IS NULL
            OR o.created_at >= :fromDate
        )
        AND (
            :toDate IS NULL
            OR o.created_at <= :toDate
        )
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM orders o
        INNER JOIN users u ON u.id = o.user_id
        WHERE o.user_id = :userId
        AND (
            :keyword IS NULL
            OR u.username LIKE CONCAT('%', :keyword, '%')
            OR u.first_name LIKE CONCAT('%', :keyword, '%')
            OR u.last_name LIKE CONCAT('%', :keyword, '%')
        )
        AND (
            :status IS NULL
            OR o.status = :status
        )
        AND (
            :paymentMethod IS NULL
            OR o.payment_method = :paymentMethod
        )
        AND (
            :fromDate IS NULL
            OR o.created_at >= :fromDate
        )
        AND (
            :toDate IS NULL
            OR o.created_at <= :toDate
        )
        """,
            nativeQuery = true
    )
    Page<Order> searchMyOrders(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("paymentMethod") String paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("userId") String userId,
            Pageable pageable
    );
}
