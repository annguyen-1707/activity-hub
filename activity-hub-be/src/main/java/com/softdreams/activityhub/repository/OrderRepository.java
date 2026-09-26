package com.softdreams.activityhub.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.softdreams.activityhub.dto.projection.OrderRevenueProjection;
import com.softdreams.activityhub.enums.OrderStatus;
import com.softdreams.activityhub.enums.PaymentMethodEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.dto.projection.OrderStatisticsProjection;
import com.softdreams.activityhub.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderLines WHERE o.id = :orderId")
    Optional<Order> findByIdWithLines(@Param("orderId") String orderId);

    boolean existsByIdAndUserId(String orderId, String userId);

    @Query(
            value = """
					SELECT o
					FROM Order o
					INNER JOIN FETCH o.user u
					WHERE u.id = :userId
					AND (
						:keyword IS NULL
						OR u.username LIKE CONCAT('%', :keyword, '%')
						OR u.firstName LIKE CONCAT('%', :keyword, '%')
						OR u.lastName LIKE CONCAT('%', :keyword, '%')
					)
					AND (
						:status IS NULL
						OR o.status = :status
					)
					AND (
						:paymentMethod IS NULL
						OR o.paymentMethod = :paymentMethod
					)
					AND (
						:fromDate IS NULL
						OR o.createdAt >= :fromDate
					)
					AND (
						:toDate IS NULL
						OR o.createdAt <= :toDate
					)
					""")
    Page<Order> searchMyOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("paymentMethod") PaymentMethodEnum paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("userId") String userId,
            Pageable pageable);

    @Query(
            """
						SELECT o
						FROM Order o
						INNER JOIN FETCH o.user u
						WHERE
						(
							:keyword IS NULL
							OR :keyword = ''
							OR u.username LIKE CONCAT('%', :keyword, '%')
							OR u.firstName LIKE CONCAT('%', :keyword, '%')
							OR u.lastName LIKE CONCAT('%', :keyword, '%')
						)
						AND (
							:status IS NULL
							OR o.status = :status
						)
						AND (
							:paymentMethod IS NULL
							OR o.paymentMethod = :paymentMethod
						)
						AND (
							:fromDate IS NULL
							OR o.createdAt >= :fromDate
						)
						AND (
							:toDate IS NULL
							OR o.createdAt <= :toDate
						)
					""")
    Page<Order> searchAllOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("paymentMethod") PaymentMethodEnum paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(
            """
			SELECT
				COUNT(o) AS totalOrders,
				COALESCE(SUM(CASE WHEN o.status != com.softdreams.activityhub.enums.OrderStatus.CANCELLED THEN o.totalAmount ELSE 0 END), 0) AS totalRevenue,
				COALESCE(SUM(CASE WHEN o.status = com.softdreams.activityhub.enums.OrderStatus.CREATED OR o.status = com.softdreams.activityhub.enums.OrderStatus.CONFIRMED THEN 1L ELSE 0L END), 0) AS pendingCount,
				COALESCE(SUM(CASE WHEN o.status = com.softdreams.activityhub.enums.OrderStatus.COMPLETED THEN 1L ELSE 0L END), 0) AS completedCount,
				COALESCE(SUM(CASE WHEN o.status = com.softdreams.activityhub.enums.OrderStatus.CANCELLED THEN 1L ELSE 0L END), 0) AS cancelledCount,
				COALESCE(SUM(CASE WHEN o.status = com.softdreams.activityhub.enums.OrderStatus.CREATED THEN 1L ELSE 0L END), 0) AS createdCount,
				COALESCE(SUM(CASE WHEN o.status = com.softdreams.activityhub.enums.OrderStatus.CONFIRMED THEN 1L ELSE 0L END), 0) AS confirmedCount
			FROM Order o
			LEFT JOIN o.user u
			WHERE
			(
				:keyword IS NULL
				OR :keyword = ''
				OR u.username LIKE CONCAT('%', :keyword, '%')
				OR u.firstName LIKE CONCAT('%', :keyword, '%')
				OR u.lastName LIKE CONCAT('%', :keyword, '%')
			)
			AND (
				:status IS NULL
				OR :status = ''
				OR :status = 'ALL'
				OR o.status = :status
			)
			AND (
				:paymentMethod IS NULL
				OR :paymentMethod = ''
				OR :paymentMethod = 'ALL'
				OR o.paymentMethod = :paymentMethod
			)
			AND (
				:fromDate IS NULL
				OR o.createdAt >= :fromDate
			)
			AND (
				:toDate IS NULL
				OR o.createdAt <= :toDate
			)
			""")
    OrderStatisticsProjection getAdminStatistics(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("paymentMethod") PaymentMethodEnum paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    @Query(
            """
			SELECT o.createdAt AS createdAt, o.totalAmount AS totalAmount
			FROM Order o
			WHERE o.status != com.softdreams.activityhub.enums.OrderStatus.CANCELLED
			  AND o.createdAt >= :fromDate
			ORDER BY o.createdAt ASC
			""")
  List<OrderRevenueProjection> findOrderRevenueSince(
            @Param("fromDate") LocalDateTime fromDate);

    @Query(
            """
			SELECT o
			FROM Order o
			INNER JOIN FETCH o.user u
			WHERE
			(
				:keyword IS NULL
				OR :keyword = ''
				OR u.username LIKE CONCAT('%', :keyword, '%')
				OR u.firstName LIKE CONCAT('%', :keyword, '%')
				OR u.lastName LIKE CONCAT('%', :keyword, '%')
			)
			AND (
				:status IS NULL
				OR o.status = :status
			)
			AND (
				:paymentMethod IS NULL
				OR o.paymentMethod = :paymentMethod
			)
			AND (
				CAST(:fromDate AS timestamp) IS NULL
				OR o.createdAt >= :fromDate
			)
			AND (
				CAST(:toDate AS timestamp) IS NULL
				OR o.createdAt <= :toDate
			)
			ORDER BY o.createdAt DESC
			""")
    List<Order> findOrdersForExport(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            @Param("paymentMethod") String paymentMethod,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate)
			;
}
