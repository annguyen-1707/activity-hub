-- =========================================================================================
-- Flyway Migration V1: Stored Procedure tạo đơn hàng nguyên tử (sp_CreateOrder)
-- =========================================================================================
CREATE OR ALTER PROCEDURE dbo.sp_CreateOrder
    @userId           VARCHAR(36),
    @shippingAddress  NVARCHAR(255) = NULL,
    @note             NVARCHAR(255) = NULL,
    @paymentMethod    VARCHAR(30)   = 'BANK',
    @itemsJson        NVARCHAR(MAX), -- JSON mảng: [{"productId": "uuid", "quantity": 2}, ...]
    @orderId          VARCHAR(36) = NULL OUTPUT,
    @totalAmount      DECIMAL(18,2) = NULL OUTPUT,
    @errorCode        INT = 0 OUTPUT,
    @errorMessage     NVARCHAR(500) = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @errorCode = 0;
    SET @errorMessage = N'Thành công';

    -- 1. Kiểm tra User tồn tại
    IF NOT EXISTS (SELECT 1 FROM users WHERE id = @userId)
    BEGIN
        SET @errorCode = 404;
        SET @errorMessage = N'Người dùng không tồn tại';
        RETURN;
    END

    -- 2. Kiểm tra chuỗi JSON đầu vào
    IF @itemsJson IS NULL OR ISJSON(@itemsJson) <> 1
    BEGIN
        SET @errorCode = 400;
        SET @errorMessage = N'Dữ liệu sản phẩm (JSON) không hợp lệ';
        RETURN;
    END

    -- 3. Tạo bảng tạm chứa danh sách sản phẩm yêu cầu
    DECLARE @OrderItems TABLE (
        productId VARCHAR(36),
        quantity INT
    );

    INSERT INTO @OrderItems (productId, quantity)
    SELECT 
        productId,
        quantity
    FROM OPENJSON(@itemsJson)
    WITH (
        productId VARCHAR(36) '$.productId',
        quantity INT '$.quantity'
    );

    IF NOT EXISTS (SELECT 1 FROM @OrderItems)
    BEGIN
        SET @errorCode = 400;
        SET @errorMessage = N'Đơn hàng phải có ít nhất 1 sản phẩm';
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM @OrderItems WHERE quantity <= 0)
    BEGIN
        SET @errorCode = 400;
        SET @errorMessage = N'Số lượng đặt mua phải lớn hơn 0';
        RETURN;
    END

    -- 4. Kiểm tra sự tồn tại của sản phẩm
    IF EXISTS (
        SELECT 1 
        FROM @OrderItems oi
        LEFT JOIN products p ON oi.productId = p.id
        WHERE p.id IS NULL
    )
    BEGIN
        SET @errorCode = 404;
        SET @errorMessage = N'Có sản phẩm trong danh sách không tồn tại trong hệ thống';
        RETURN;
    END

    -- 5. Kiểm tra số lượng tồn kho từng sản phẩm
    DECLARE @insufficientProductName NVARCHAR(255);
    DECLARE @availableQty INT;
    DECLARE @requestedQty INT;

    SELECT TOP 1 
        @insufficientProductName = p.name,
        @availableQty = p.quantity,
        @requestedQty = oi.quantity
    FROM @OrderItems oi
    INNER JOIN products p ON oi.productId = p.id
    WHERE p.quantity < oi.quantity;

    IF @insufficientProductName IS NOT NULL
    BEGIN
        SET @errorCode = 400;
        SET @errorMessage = N'Sản phẩm [' + @insufficientProductName + N'] không đủ số lượng tồn kho (Còn: ' 
                            + CAST(@availableQty AS NVARCHAR(20)) + N', yêu cầu: ' 
                            + CAST(@requestedQty AS NVARCHAR(20)) + N')';
        RETURN;
    END

    -- 6. Tính tổng tiền đơn hàng
    SELECT @totalAmount = SUM(p.price * oi.quantity)
    FROM @OrderItems oi
    INNER JOIN products p ON oi.productId = p.id;

    IF @orderId IS NULL OR LEN(@orderId) = 0
        SET @orderId = LOWER(CONVERT(VARCHAR(36), NEWID()));

    DECLARE @stockTxId VARCHAR(36) = LOWER(CONVERT(VARCHAR(36), NEWID()));
    DECLARE @now DATETIME2 = SYSUTCDATETIME();

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 7. Thêm đơn hàng header
        INSERT INTO orders (
            id, user_id, total_amount, status, created_at, completed_at, payment_method, shipping_address, note
        )
        VALUES (
            @orderId, @userId, @totalAmount, 'CREATED', @now, NULL, @paymentMethod, @shippingAddress, @note
        );

        -- 8. Thêm chi tiết đơn hàng (order_lines)
        INSERT INTO order_lines (
            id, order_id, product_id, quantity, unit_price, subtotal
        )
        SELECT 
            LOWER(CONVERT(VARCHAR(36), NEWID())),
            @orderId,
            oi.productId,
            oi.quantity,
            p.price,
            (p.price * oi.quantity)
        FROM @OrderItems oi
        INNER JOIN products p ON oi.productId = p.id;

        -- 9. Trừ tồn kho sản phẩm
        UPDATE p
        SET p.quantity = p.quantity - oi.quantity,
            p.updated_at = @now
        FROM products p
        INNER JOIN @OrderItems oi ON p.id = oi.productId;

        -- 10. Ghi log giao dịch biến động kho xuất bán (Loại SALE)
        INSERT INTO stock_transactions (
            id, type, reference_id, note, created_by, created_at
        )
        VALUES (
            @stockTxId,
            'SALE',
            @orderId,
            N'Tự động xuất kho cho đơn hàng #' + LEFT(@orderId, 8),
            @userId,
            @now
        );

        -- 11. Ghi chi tiết biến động kho (-quantity)
        INSERT INTO stock_transaction_lines (
            id, stock_transaction_id, product_id, quantity
        )
        SELECT 
            LOWER(CONVERT(VARCHAR(36), NEWID())),
            @stockTxId,
            oi.productId,
            -oi.quantity
        FROM @OrderItems oi;

        COMMIT TRANSACTION;

        SET @errorCode = 0;
        SET @errorMessage = N'Tạo đơn hàng thành công';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        SET @errorCode = ERROR_NUMBER();
        SET @errorMessage = ERROR_MESSAGE();
    END CATCH
END;

