package com.softdreams.activityhub.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.softdreams.activityhub.enums.OrderStatus;
import com.softdreams.activityhub.enums.PaymentMethodEnum;
import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "payment_method", length = 30)
    PaymentMethodEnum paymentMethod;

    @Column(name = "shipping_address", length = 500, columnDefinition = "NVARCHAR(255)")
    String shippingAddress;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(255)")
    String note;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @BatchSize(size = 50)
    private List<OrderLine> orderLines;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
