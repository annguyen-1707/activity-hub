package com.softdreams.activityhub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.dto.response.OrderResponse;
import com.softdreams.activityhub.entity.Order;
import com.softdreams.activityhub.entity.OrderLine;
import com.softdreams.activityhub.entity.Product;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.enums.OrderStatus;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.OrderMapper;
import com.softdreams.activityhub.repository.OrderLineRepository;
import com.softdreams.activityhub.repository.OrderRepository;
import com.softdreams.activityhub.repository.ProductRepository;
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
    ProductRepository productRepository;
    StockTransactionService stockTransactionService;

    private User getMyUser() {
        User user;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return user;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = getMyUser();

        Order order = orderMapper.toOrder(request);
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        List<OrderLine> orderLines = new ArrayList<>();
        List<StockTransactionService.StockLine> stockLines = new ArrayList<>();

        for (var item : request.getItems()) {
            // Locked here so two concurrent checkouts can't both oversell the same product.
            Product product = productRepository
                    .findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            orderLines.add(OrderLine.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build());

            stockLines.add(new StockTransactionService.StockLine(product, item.getQuantity()));
        }

        order.setOrderLines(orderLines);

        BigDecimal total = orderLines.stream().map(OrderLine::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        // Deducts stock for every line; rolls back the whole order if any product is short.
        Order saved = orderRepository.save(order);
        stockTransactionService.postSale(saved.getId(), stockLines);

        return orderMapper.toOrderResponse(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)")
    public OrderResponse getById(String orderId) {
        Order order = orderRepository
                .findByIdWithLines(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
        return orderMapper.toOrderResponse(order);
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)")
    public OrderResponse update(String orderId, OrderRequest request) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        orderMapper.updateOrder(order, request);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    public Page<OrderResponse> searchMyOrders(
            String keyword,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        User user = getMyUser();
        return orderRepository
                .searchMyOrders(keyword, status, paymentMethod, fromDate, toDate, user.getId(), pageable)
                .map(orderMapper::toOrderResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> searchAdminOrders(
            String keyword,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        return orderRepository
                .searchAllOrders(keyword, status, paymentMethod, fromDate, toDate, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse approve(String orderId) {
        Order order = orderRepository
                .findByIdWithLines(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        order.setStatus(OrderStatus.CONFIRMED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse done(String orderId) {
        Order order = orderRepository
                .findByIdWithLines(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        order.setStatus(OrderStatus.COMPLETED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse reject(String orderId) {
        Order order = orderRepository
                .findByIdWithLines(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus != OrderStatus.CANCELLED) {
            List<StockTransactionService.StockLine> stockLines = order.getOrderLines().stream()
                    .map(line -> new StockTransactionService.StockLine(line.getProduct(), line.getQuantity()))
                    .toList();
            stockTransactionService.postCancel(order.getId(), stockLines);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)")
    @Transactional
    public OrderResponse cancel(String orderId) {
        Order order = orderRepository
                .findByIdWithLines(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus != OrderStatus.CANCELLED) {
            List<StockTransactionService.StockLine> stockLines = order.getOrderLines().stream()
                    .map(line -> new StockTransactionService.StockLine(line.getProduct(), line.getQuantity()))
                    .toList();
            stockTransactionService.postCancel(order.getId(), stockLines);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateStatus(String orderId, OrderStatus newStatus) {
        return switch (newStatus) {
            case CONFIRMED -> approve(orderId);
            case COMPLETED -> done(orderId);
            case CANCELLED -> cancel(orderId);
            default -> {
                Order order = orderRepository
                        .findByIdWithLines(orderId)
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));
                order.setStatus(newStatus);
                yield orderMapper.toOrderResponse(orderRepository.save(order));
            }
        };
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)")
    public void delete(String id) {
        if (!orderRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_NOT_EXISTED);
        }
        orderRepository.deleteById(id);
    }
}
