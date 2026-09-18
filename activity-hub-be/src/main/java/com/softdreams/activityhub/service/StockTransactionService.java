package com.softdreams.activityhub.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.request.StockTransactionRequest;
import com.softdreams.activityhub.dto.response.StockTransactionResponse;
import com.softdreams.activityhub.entity.Product;
import com.softdreams.activityhub.entity.StockTransaction;
import com.softdreams.activityhub.entity.StockTransactionLine;
import com.softdreams.activityhub.entity.User;
import com.softdreams.activityhub.enums.StockTransactionType;
import com.softdreams.activityhub.exception.AppException;
import com.softdreams.activityhub.exception.ErrorCode;
import com.softdreams.activityhub.mapper.StockTransactionMapper;
import com.softdreams.activityhub.repository.ProductRepository;
import com.softdreams.activityhub.repository.StockTransactionRepository;
import com.softdreams.activityhub.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockTransactionService {
    StockTransactionRepository stockTransactionRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    StockTransactionMapper stockTransactionMapper;

    /** A stock movement not yet posted: how many units of a product move, and in which direction. */
    public record StockLine(Product product, int quantity) {}

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StockTransactionResponse createManual(StockTransactionRequest request) {
        if (request.getType() != StockTransactionType.IMPORT && request.getType() != StockTransactionType.ADJUSTMENT) {
            throw new AppException(ErrorCode.INVALID_STOCK_TRANSACTION_TYPE);
        }

        List<PendingLine> pending = request.getLines().stream()
                .map(line -> {
                    Product product = productRepository
                            .findByIdForUpdate(line.getProductId())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

                    int delta = request.getType() == StockTransactionType.IMPORT
                            ? Math.abs(line.getQuantity())
                            : line.getQuantity();

                    return new PendingLine(product, delta);
                })
                .toList();

        return apply(request.getType(), request.getNote(), null, pending);
    }

    /**
     * Deducts stock for a checkout. Called from OrderService inside the same
     * transaction as the order insert, so a rollback undoes both together.
     */
    @Transactional
    public StockTransactionResponse postSale(String orderId, List<StockLine> lines) {
        List<PendingLine> pending =
                lines.stream().map(l -> new PendingLine(l.product(), -l.quantity())).toList();

        return apply(StockTransactionType.SALE, "Auto-generated from order checkout", orderId, pending);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<StockTransactionResponse> search(StockTransactionType type, String productId, Pageable pageable) {
        return stockTransactionRepository
                .search(type, productId, pageable)
                .map(stockTransactionMapper::toResponse);
    }

    private record PendingLine(Product product, int delta) {}

    private StockTransactionResponse apply(
            StockTransactionType type, String note, String referenceId, List<PendingLine> pendingLines) {
        StockTransaction header = StockTransaction.builder()
                .type(type)
                .note(note)
                .referenceId(referenceId)
                .createdBy(getMyUser())
                .build();

        List<StockTransactionLine> lines = new ArrayList<>();
        for (PendingLine pending : pendingLines) {
            int newQuantity = pending.product().getQuantity() + pending.delta();
            if (newQuantity < 0) {
                throw new AppException(
                        ErrorCode.INSUFFICIENT_STOCK,
                        "Insufficient stock for product: " + pending.product().getName());
            }

            pending.product().setQuantity(newQuantity);
            lines.add(StockTransactionLine.builder()
                    .stockTransaction(header)
                    .product(pending.product())
                    .quantity(pending.delta())
                    .build());
        }
        header.setLines(lines);

        productRepository.saveAll(
                pendingLines.stream().map(PendingLine::product).toList());
        StockTransaction saved = stockTransactionRepository.save(header);

        return stockTransactionMapper.toResponse(saved);
    }

    private User getMyUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}
