package com.softdreams.activityhub.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.softdreams.activityhub.dto.report.StockTransactionReportItem;
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
import com.softdreams.activityhub.repository.StockTransactionLineRepository;
import com.softdreams.activityhub.repository.StockTransactionRepository;
import com.softdreams.activityhub.repository.UserRepository;

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
    StockTransactionLineRepository stockTransactionLineRepository;
    JasperReportService jasperReportService;

    public record StockLine(Product product, int quantity) {}

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StockTransactionResponse createManual(StockTransactionRequest request) {

        List<PendingLine> pending = request.getLines().stream()
                .map(line -> {
                    Product product = productRepository
                            .findByIdForUpdate(line.getProductId())
                            .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

                    int delta = StockTransactionType.IMPORT.equals(request.getType())
                            ? Math.abs(line.getQuantity())
                            : line.getQuantity();

                    return new PendingLine(product, delta);
                })
                .toList();

        return apply(request.getType(), request.getNote(), null, pending);
    }

    @Transactional
    public StockTransactionResponse postSale(String orderId, List<StockLine> lines) {
        List<PendingLine> pending = lines.stream()
                .map(l -> new PendingLine(l.product(), -l.quantity()))
                .toList();

        return apply(StockTransactionType.SALE, "Auto-generated from order checkout", orderId, pending);
    }

    @Transactional
    public StockTransactionResponse postCancel(String orderId, List<StockLine> lines) {
        List<PendingLine> pending = lines.stream()
                .map(l -> new PendingLine(l.product(), l.quantity()))
                .toList();

        return apply(StockTransactionType.CANCEL, "Auto-restored from order cancellation", orderId, pending);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<StockTransactionResponse> search(
            StockTransactionType type, String productId, LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        Page<StockTransaction> page = stockTransactionRepository.search(type, productId, fromDate, toDate, pageable);

        List<String> transactionIds =
                page.getContent().stream().map(StockTransaction::getId).toList();

        List<StockTransactionLine> lines = transactionIds.isEmpty()
                ? List.of()
                : stockTransactionLineRepository.findLinesWithProduct(transactionIds);

        Map<String, List<StockTransactionLine>> linesByTransaction = lines.stream()
                .collect(
                        Collectors.groupingBy(line -> line.getStockTransaction().getId()));
        return page.map(transaction -> {
            transaction.setLines(linesByTransaction.getOrDefault(transaction.getId(), List.of()));
            return stockTransactionMapper.toResponse(transaction);
        });
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<StockTransactionResponse> getTransactionsForExport(
            StockTransactionType type, String productId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<StockTransaction> list = stockTransactionRepository.findForExport(type, productId, fromDate, toDate);

        List<String> transactionIds = list.stream().map(StockTransaction::getId).toList();

        List<StockTransactionLine> lines = transactionIds.isEmpty()
                ? List.of()
                : stockTransactionLineRepository.findLinesWithProduct(transactionIds);

        Map<String, List<StockTransactionLine>> linesByTransaction = lines.stream()
                .collect(
                        Collectors.groupingBy(line -> line.getStockTransaction().getId()));
        return list.stream().map(transaction -> {
            transaction.setLines(linesByTransaction.getOrDefault(transaction.getId(), List.of()));
            return stockTransactionMapper.toResponse(transaction);
        }).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public byte[] exportPdf(StockTransactionType type, String productId, LocalDateTime fromDate, LocalDateTime toDate) {
        List<StockTransactionResponse> list = getTransactionsForExport(type, productId, fromDate, toDate);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter displayDtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        AtomicInteger counter = new AtomicInteger(1);
        List<StockTransactionReportItem> reportItems = list.stream().map(tx -> {
            String prodSummary = tx.getLines() != null && !tx.getLines().isEmpty()
                    ? tx.getLines().stream()
                            .map(l -> l.getProductName() + " (" + (l.getQuantity() > 0 ? "+" + l.getQuantity() : l.getQuantity()) + ")")
                            .collect(Collectors.joining(", "))
                    : "—";

            int totalQty = tx.getLines() != null
                    ? tx.getLines().stream().mapToInt(com.softdreams.activityhub.dto.response.StockTransactionLineResponse::getQuantity).sum()
                    : 0;

            String typeStr = tx.getType() != null ? tx.getType().name() : "";
            String typeLabel = switch (typeStr) {
                case "IMPORT" -> "Nhập kho";
                case "EXPORT" -> "Xuất kho";
                case "SALE" -> "Bán hàng";
                case "CANCEL" -> "Hủy đơn";
                case "RETURN" -> "Trả hàng";
                default -> typeStr;
            };

            return StockTransactionReportItem.builder()
                    .stt(counter.getAndIncrement())
                    .transactionId(tx.getId() != null ? "#" + tx.getId().substring(0, Math.min(8, tx.getId().length())) : "")
                    .typeLabel(typeLabel)
                    .referenceId(tx.getReferenceId() != null ? tx.getReferenceId() : "")
                    .productName(prodSummary)
                    .quantity(totalQty > 0 ? "+" + totalQty : String.valueOf(totalQty))
                    .createdByName(tx.getCreatedByUsername() != null ? tx.getCreatedByUsername() : "Hệ thống")
                    .createdAt(tx.getCreatedAt() != null ? tx.getCreatedAt().format(dtf) : "")
                    .note(tx.getNote() != null ? tx.getNote() : "")
                    .build();
        }).toList();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "BÁO CÁO BIẾN ĐỘNG KHO");

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
        parameters.put("printedBy", currentUser != null ? currentUser.getUsername() : "Admin");
        parameters.put("printedAt", LocalDateTime.now().format(dtf));
        parameters.put("totalRecords", reportItems.size());

        return jasperReportService.exportToPdf("stock_transactions_report", parameters, reportItems);
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
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }
}
