package com.softdreams.activityhub.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.projection.OrderLineProjection;
import com.softdreams.activityhub.dto.projection.OrderStatisticsProjection;
import com.softdreams.activityhub.dto.report.OrderReportItem;
import com.softdreams.activityhub.dto.request.OrderRequest;
import com.softdreams.activityhub.dto.response.OrderLineResponse;
import com.softdreams.activityhub.dto.response.OrderResponse;
import com.softdreams.activityhub.dto.response.OrderStatisticsResponse;
import com.softdreams.activityhub.dto.response.ReviewResponse;
import com.softdreams.activityhub.entity.Order;
import com.softdreams.activityhub.entity.OrderLine;
import com.softdreams.activityhub.entity.Product;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.enums.OrderStatus;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.OrderMapper;
import com.softdreams.activityhub.mapper.ReviewMapper;
import com.softdreams.activityhub.repository.OrderLineRepository;
import com.softdreams.activityhub.repository.OrderRepository;
import com.softdreams.activityhub.repository.ProductRepository;
import com.softdreams.activityhub.repository.ReviewRepository;
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
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;
    JasperReportService jasperReportService;

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
        Page<Order> orderPages = orderRepository.searchMyOrders(
                keyword, status, paymentMethod, fromDate, toDate, user.getId(), pageable);
        return mapOrdersWithOrderLines(orderPages);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> searchAdminOrders(
            String keyword,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {
        Page<Order> orderPages =
                orderRepository.searchAllOrders(keyword, status, paymentMethod, fromDate, toDate, pageable);
        return mapOrdersWithOrderLines(orderPages);
    }

    public byte[] exportMyOrdersPdf(
            String keyword,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        User user = getMyUser();
        List<Order> orders = orderRepository.findOrdersForExport(
                keyword, status, paymentMethod, fromDate, toDate, user.getId());
        return generateOrdersReport(orders, "LỊCH SỬ ĐƠN HÀNG CỦA TÔI", fromDate, toDate);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public byte[] exportAdminOrdersPdf(
            String keyword,
            String status,
            String paymentMethod,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        List<Order> orders = orderRepository.findOrdersForExport(
                keyword, status, paymentMethod, fromDate, toDate, null);
        return generateOrdersReport(orders, "BÁO CÁO QUẢN LÝ ĐƠN HÀNG", fromDate, toDate);
    }

    private byte[] generateOrdersReport(
            List<Order> orders,
            String title,
            LocalDateTime fromDate,
            LocalDateTime toDate) {
        List<String> orderIds = orders.stream().map(Order::getId).toList();
        Map<String, List<OrderLineResponse>> linesByOrderId = orderIds.isEmpty()
                ? Map.of()
                : orderLineRepository.getOrderLinesByOrderIds(orderIds).stream()
                        .collect(Collectors.groupingBy(
                                OrderLineProjection::getOrderId,
                                Collectors.mapping(
                                        p -> new OrderLineResponse(
                                                p.getId(),
                                                p.getProductId(),
                                                p.getProductName(),
                                                p.getQuantity(),
                                                p.getUnitPrice(),
                                                p.getSubtotal()),
                                        Collectors.toList())));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter displayDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

        AtomicInteger counter = new AtomicInteger(1);
        List<OrderReportItem> reportItems = orders.stream().map(order -> {
            List<OrderLineResponse> lines = linesByOrderId.getOrDefault(order.getId(), List.of());
            String productSummary = lines.stream()
                    .map(l -> l.getProductName() + " (x" + l.getQuantity() + ")")
                    .collect(Collectors.joining(", "));

            String statusLabel = order.getStatus() != null ? switch (order.getStatus()) {
                case CREATED -> "Chờ xác nhận";
                case CONFIRMED -> "Đã xác nhận";
                case COMPLETED -> "Hoàn thành";
                case CANCELLED -> "Đã hủy";
            } : "";

            String customerName = order.getCustomerName();
            if ((customerName == null || customerName.isBlank()) && order.getUser() != null) {
            String customerName = "";
            if (order.getUser() != null) {
                customerName = (order.getUser().getFirstName() != null ? order.getUser().getFirstName() + " " : "")
                        + (order.getUser().getLastName() != null ? order.getUser().getLastName() : "");
                if (customerName.isBlank() && order.getUser().getUsername() != null) {
                    customerName = order.getUser().getUsername();
                }
            }

            return OrderReportItem.builder()
                    .stt(counter.getAndIncrement())
                    .orderId("#" + order.getId().substring(0, Math.min(8, order.getId().length())))
                    .customerName(customerName != null ? customerName.trim() : "")
                    .customerName(customerName.trim())
                    .productSummary(productSummary.isEmpty() ? "—" : productSummary)
                    .totalAmount(order.getTotalAmount() != null ? currencyFormat.format(order.getTotalAmount()) + " đ" : "0 đ")
                    .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD")
                    .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD")
                    .statusLabel(statusLabel)
                    .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().format(dtf) : "")
                    .shippingAddress(order.getShippingAddress() != null ? order.getShippingAddress() : "")
                    .build();
        }).toList();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", title);

        String timeFilter = "Khoảng thời gian: ";
        if (fromDate != null && toDate != null) {
            timeFilter += "Từ " + fromDate.format(displayDtf) + " đến " + toDate.format(displayDtf);
        } else if (fromDate != null) {
            timeFilter += "Từ " + fromDate.format(displayDtf);
        } else if (toDate != null) {
            timeFilter += "Đến " + toDate.format(displayDtf);
        } else {
            timeFilter += "Toàn bộ thời gian";
        }
        parameters.put("filterInfo", timeFilter);

        User currentUser = getMyUser();
        parameters.put("printedBy", currentUser != null ? currentUser.getUsername() : "Hệ thống");
        parameters.put("printedAt", LocalDateTime.now().format(dtf));
        parameters.put("totalRecords", reportItems.size());

        return jasperReportService.exportToPdf("orders_report", parameters, reportItems);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public OrderStatisticsResponse getAdminStatistics() {
        OrderStatisticsProjection proj = orderRepository.getAdminStatistics();
        return toStatisticsResponse(proj);
    }

    private OrderStatisticsResponse toStatisticsResponse(OrderStatisticsProjection proj) {
        if (proj == null) {
            return OrderStatisticsResponse.builder()
                    .totalOrders(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .pendingCount(0)
                    .completedCount(0)
                    .cancelledCount(0)
                    .createdCount(0)
                    .confirmedCount(0)
                    .build();
        }
        return OrderStatisticsResponse.builder()
                .totalOrders(proj.getTotalOrders() != null ? proj.getTotalOrders() : 0)
                .totalRevenue(proj.getTotalRevenue() != null ? proj.getTotalRevenue() : BigDecimal.ZERO)
                .pendingCount(proj.getPendingCount() != null ? proj.getPendingCount() : 0)
                .completedCount(proj.getCompletedCount() != null ? proj.getCompletedCount() : 0)
                .cancelledCount(proj.getCancelledCount() != null ? proj.getCancelledCount() : 0)
                .createdCount(proj.getCreatedCount() != null ? proj.getCreatedCount() : 0)
                .confirmedCount(proj.getConfirmedCount() != null ? proj.getConfirmedCount() : 0)
                .build();
    }

    private Page<OrderResponse> mapOrdersWithOrderLines(Page<Order> orderPage) {
        List<String> orderIds =
                orderPage.getContent().stream().map(Order::getId).toList();

        if (orderIds.isEmpty()) {
            return orderPage.map(orderMapper::toOrderResponse);
        }

        Map<String, List<OrderLineResponse>> linesByOrderId =
                orderLineRepository.getOrderLinesByOrderIds(orderIds).stream()
                        .collect(Collectors.groupingBy(
                                OrderLineProjection::getOrderId,
                                Collectors.mapping(
                                        p -> new OrderLineResponse(
                                                p.getId(),
                                                p.getProductId(),
                                                p.getProductName(),
                                                p.getQuantity(),
                                                p.getUnitPrice(),
                                                p.getSubtotal()),
                                        Collectors.toList())));
        return orderPage.map(order -> {
            OrderResponse response = orderMapper.toOrderResponse(order);
            response.setOrderLines(linesByOrderId.getOrDefault(order.getId(), List.of()));
            return response;
        });
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
        order.setCompletedAt(LocalDateTime.now());
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
    public List<ReviewResponse> getReviewsForOrder(String orderId) {
        Order order = orderRepository
                .findByIdWithLines(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        List<String> lineIds =
                order.getOrderLines().stream().map(OrderLine::getId).toList();

        if (lineIds.isEmpty()) {
            return List.of();
        }

        return reviewRepository.findByOrderLine_IdIn(lineIds).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @security.isOrderOwner(#orderId, authentication)")
    public void delete(String id) {
        if (!orderRepository.existsById(id)) {
            throw new AppException(ErrorCode.ORDER_NOT_EXISTED);
        }
        orderRepository.deleteById(id);
    }
}
