package com.softdreams.activityhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.softdreams.activityhub.dto.response.OrderLineResponse;
import com.softdreams.activityhub.entity.OrderLine;
import com.softdreams.activityhub.enums.OrderStatus;
import com.softdreams.activityhub.repository.OrderLineRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.dto.response.OrderResponse;
import com.softdreams.activityhub.entity.Order;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.OrderMapper;
import com.softdreams.activityhub.repository.OrderRepository;
import com.softdreams.activityhub.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {
    OrderRepository orderRepository;
    UserRepository userRepository;
    OrderMapper orderMapper;
    OrderLineRepository orderLineRepository;

    private User getMyUser() {
        User user;
        String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return user;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = getMyUser();

        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        List<OrderLine> orderLines = request.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal =
                            item.getUnitPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()));

                    return OrderLine.builder()
                            .order(order)
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();

        order.setOrderLines(orderLines);

        BigDecimal total = orderLines.stream()
                .map(OrderLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        return orderMapper.toOrderResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }
    
    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)" )
    public OrderResponse getById(String orderId) {
        Order order = orderRepository.findByIdWithLines(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)" )
    public OrderResponse update(String orderId, OrderRequest request) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        orderMapper.updateOrder(order, request);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    public Page<OrderResponse> searchMyOrders (String keyword,String status,String paymentMethod,
                                                LocalDateTime fromDate,LocalDateTime toDate, Pageable pageable) {
        User user = getMyUser();
        return orderRepository.searchMyOrders(keyword, status, paymentMethod, fromDate, toDate, user.getId(), pageable).map(orderMapper::toOrderResponse);
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)" )
    public void delete(String id) {
        if (!orderRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_NOT_EXISTED);
        }
        orderRepository.deleteById(id);
    }
}
