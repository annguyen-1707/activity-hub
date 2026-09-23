-- =========================================================================================
-- Flyway Migration V5: Điều chỉnh múi giờ Việt Nam (UTC+7 / GMT+7) cho các Stored Procedure
-- Nguyên nhân: SQL Server trong Docker container chạy múi giờ chuẩn UTC (0h),
--              dẫn đến thời gian tạo đơn bị lùi 7 tiếng so với giờ thực tế ở Việt Nam.
-- =========================================================================================

-- 1. CẬP NHẬT sp_CreateOrder
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
    -- Sử dụng giờ Việt Nam (UTC+7)
    DECLARE @now DATETIME2 = DATEADD(HOUR, 7, SYSUTCDATETIME());

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
GO

-- 2. CẬP NHẬT sp_CancelOrder
CREATE OR ALTER PROCEDURE dbo.sp_CancelOrder
    @orderId        VARCHAR(36),
    @cancelledBy    VARCHAR(36) = NULL, -- User ID thực hiện hủy
    @errorCode      INT = 0 OUTPUT,
    @errorMessage   NVARCHAR(500) = NULL OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    SET @errorCode = 0;
    SET @errorMessage = N'Thành công';

    -- 1. Kiểm tra đơn hàng tồn tại
    DECLARE @currentStatus VARCHAR(30);
    SELECT @currentStatus = status 
    FROM orders 
    WHERE id = @orderId;

    IF @currentStatus IS NULL
    BEGIN
        SET @errorCode = 404;
        SET @errorMessage = N'Đơn hàng không tồn tại';
        RETURN;
    END

    -- 2. Kiểm tra nếu đơn hàng đã bị hủy trước đó
    IF @currentStatus = 'CANCELLED'
    BEGIN
        SET @errorCode = 400;
        SET @errorMessage = N'Đơn hàng đã ở trạng thái hủy trước đó';
        RETURN;
    END

    -- Sử dụng giờ Việt Nam (UTC+7)
    DECLARE @now DATETIME2 = DATEADD(HOUR, 7, SYSUTCDATETIME());

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 3. Cập nhật trạng thái đơn hàng sang CANCELLED
        UPDATE orders
        SET status = 'CANCELLED'
        WHERE id = @orderId;

        -- 4. Hoàn trả số lượng tồn kho cho các sản phẩm trong đơn hàng
        UPDATE p
        SET p.quantity = p.quantity + ol.quantity,
            p.updated_at = @now
        FROM products p
        INNER JOIN order_lines ol ON p.id = ol.product_id
        WHERE ol.order_id = @orderId;

        -- 5. Tạo bản ghi giao dịch biến động kho hoàn hàng (Loại CANCEL)
        DECLARE @stockTxId VARCHAR(36) = LOWER(CONVERT(VARCHAR(36), NEWID()));

        INSERT INTO stock_transactions (
            id, type, reference_id, note, created_by, created_at
        )
        VALUES (
            @stockTxId,
            'CANCEL',
            @orderId,
            N'Tự động hoàn kho khi hủy đơn hàng #' + LEFT(@orderId, 8),
            @cancelledBy,
            @now
        );

        -- 6. Ghi chi tiết biến động kho (+quantity)
        INSERT INTO stock_transaction_lines (
            id, stock_transaction_id, product_id, quantity
        )
        SELECT 
            LOWER(CONVERT(VARCHAR(36), NEWID())),
            @stockTxId,
            ol.product_id,
            ol.quantity
        FROM order_lines ol
        WHERE ol.order_id = @orderId;

        COMMIT TRANSACTION;

        SET @errorCode = 0;
        SET @errorMessage = N'Hủy đơn hàng và hoàn tồn kho thành công';
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        SET @errorCode = ERROR_NUMBER();
        SET @errorMessage = ERROR_MESSAGE();
    END CATCH
END;
GO

-- 3. CẬP NHẬT sp_GetDashboardOverview
CREATE OR ALTER PROCEDURE dbo.sp_GetDashboardOverview
    @rangeDays INT = 30,
    @fromDate DATETIME2 = NULL,
    @toDate   DATETIME2 = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Lấy giờ hiện tại Việt Nam (UTC+7)
    DECLARE @now DATETIME2 = DATEADD(HOUR, 7, SYSUTCDATETIME());

    -- Nếu không truyền fromDate, toDate thì tính theo @rangeDays gần nhất
    IF @fromDate IS NULL
        SET @fromDate = DATEADD(DAY, -@rangeDays, CAST(CONVERT(DATE, @now) AS DATETIME2));
    IF @toDate IS NULL
        SET @toDate = @now;

    -- RESULT SET 1: THẺ TỔNG QUAN KPI
    SELECT 
        COUNT(o.id) AS totalOrders,
        COALESCE(SUM(CASE WHEN o.status != 'CANCELLED' THEN o.total_amount ELSE 0 END), 0) AS totalRevenue,
        COALESCE(SUM(CASE WHEN o.status IN ('CREATED', 'CONFIRMED') THEN 1 ELSE 0 END), 0) AS pendingCount,
        COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completedCount,
        COALESCE(SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelledCount,
        COALESCE(SUM(CASE WHEN o.status = 'CREATED' THEN 1 ELSE 0 END), 0) AS createdCount,
        COALESCE(SUM(CASE WHEN o.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) AS confirmedCount
    FROM orders o
    WHERE o.created_at >= @fromDate AND o.created_at <= @toDate;

    -- RESULT SET 2: DOANH THU THEO TỪNG NGÀY
    WITH DateRange AS (
        SELECT CAST(CONVERT(DATE, @fromDate) AS DATE) AS [Date]
        UNION ALL
        SELECT DATEADD(DAY, 1, [Date])
        FROM DateRange
        WHERE [Date] < CAST(CONVERT(DATE, @toDate) AS DATE)
    )
    SELECT 
        CONVERT(VARCHAR(10), d.[Date], 120) AS orderDate,
        COALESCE(SUM(o.total_amount), 0) AS dailyRevenue,
        COUNT(o.id) AS dailyOrders
    FROM DateRange d
    LEFT JOIN orders o ON CONVERT(DATE, o.created_at) = d.[Date] 
                      AND o.status != 'CANCELLED'
    GROUP BY d.[Date]
    ORDER BY d.[Date] ASC
    OPTION (MAXRECURSION 1000);

    -- RESULT SET 3: TOP 5 SẢN PHẨM BÁN CHẠY NHẤT
    SELECT TOP 5
        p.id AS productId,
        p.name AS productName,
        p.image AS productImage,
        p.price AS price,
        c.name AS categoryName,
        COALESCE(SUM(ol.quantity), 0) AS totalSold,
        COALESCE(SUM(ol.subtotal), 0) AS totalRevenue
    FROM order_lines ol
    INNER JOIN orders o ON ol.order_id = o.id
    INNER JOIN products p ON ol.product_id = p.id
    LEFT JOIN categories c ON p.category_id = c.id
    WHERE o.status != 'CANCELLED'
      AND o.created_at >= @fromDate AND o.created_at <= @toDate
    GROUP BY p.id, p.name, p.image, p.price, c.name
    ORDER BY totalSold DESC;

    -- RESULT SET 4: CẢNH BÁO TỒN KHO THẤP (TỒN <= 10)
    SELECT TOP 10
        p.id,
        p.name,
        p.price,
        p.quantity,
        c.name AS categoryName
    FROM products p
    LEFT JOIN categories c ON p.category_id = c.id
    WHERE p.quantity <= 10
    ORDER BY p.quantity ASC;
END;
GO

