package com.softdreams.activityhub.service;

import java.time.LocalDateTime;
import java.util.List;

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

    private User getMyUser() {
        User user;
        String username =
                SecurityContextHolder.getContext().getAuthentication().getName();
        user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return user;
    }

    public OrderResponse create(OrderRequest request) {
        Order order = orderMapper.toOrder(request);
        User user = getMyUser();
        order.setUser(user);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }
    
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId, authentication)" )
    public OrderResponse getById(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId, authentication)" )
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

    public void delete(String id) {
        if (!orderRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_NOT_EXISTED);
        }
        orderRepository.deleteById(id);
    }
}
