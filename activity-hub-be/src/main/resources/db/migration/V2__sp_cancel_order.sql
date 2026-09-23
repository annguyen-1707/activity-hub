-- =========================================================================================
-- Flyway Migration V2: Stored Procedure hủy đơn hàng và hoàn tồn kho nguyên tử (sp_CancelOrder)
-- =========================================================================================
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

    BEGIN TRY
        BEGIN TRANSACTION;

        -- 3. Cập nhật trạng thái đơn hàng sang CANCELLED
        UPDATE orders
        SET status = 'CANCELLED'
        WHERE id = @orderId;

        -- 4. Hoàn trả số lượng tồn kho cho các sản phẩm trong đơn hàng
        UPDATE p
        SET p.quantity = p.quantity + ol.quantity,
            p.updated_at = SYSUTCDATETIME()
        FROM products p
        INNER JOIN order_lines ol ON p.id = ol.product_id
        WHERE ol.order_id = @orderId;

        -- 5. Tạo bản ghi giao dịch biến động kho hoàn hàng (Loại CANCEL)
        DECLARE @stockTxId VARCHAR(36) = LOWER(CONVERT(VARCHAR(36), NEWID()));
        DECLARE @now DATETIME2 = SYSUTCDATETIME();

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

