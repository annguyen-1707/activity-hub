-- =========================================================================================
-- Flyway Migration V4: Chuyển đổi các cột Enum từ SMALLINT sang VARCHAR (String)
-- Đảm bảo tương thích hoàn toàn với @Enumerated(EnumType.STRING) trong JPA
-- =========================================================================================

-- 1. XỬ LÝ BẢNG ORDERS (status và payment_method)
-- Xóa check constraint cũ do Hibernate sinh ra (nếu có, ví dụ [status]>=(0) AND [status]<=(3))
DECLARE @sqlOrders NVARCHAR(MAX) = N'';
SELECT @sqlOrders += N'ALTER TABLE orders DROP CONSTRAINT ' + QUOTENAME(con.name) + N';'
FROM sys.check_constraints con
WHERE con.parent_object_id = OBJECT_ID('orders')
  AND con.parent_column_id IN (
      SELECT column_id FROM sys.columns 
      WHERE object_id = OBJECT_ID('orders') AND name IN ('status', 'payment_method')
  );

IF LEN(@sqlOrders) > 0
    EXEC sp_executesql @sqlOrders;

-- Chuyển kiểu dữ liệu sang VARCHAR
IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'status' 
      AND DATA_TYPE IN ('smallint', 'tinyint', 'int')
)
BEGIN
    ALTER TABLE orders ALTER COLUMN status VARCHAR(30) NOT NULL;
    
    -- Cập nhật dữ liệu từ ordinal số sang tên enum String
    UPDATE orders SET status = CASE status
        WHEN '0' THEN 'CREATED'
        WHEN '1' THEN 'CONFIRMED'
        WHEN '2' THEN 'COMPLETED'
        WHEN '3' THEN 'CANCELLED'
        ELSE status
    END;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'orders' AND COLUMN_NAME = 'payment_method' 
      AND DATA_TYPE IN ('smallint', 'tinyint', 'int')
)
BEGIN
    ALTER TABLE orders ALTER COLUMN payment_method VARCHAR(30) NULL;
    
    -- Cập nhật dữ liệu từ ordinal số sang tên enum String
    UPDATE orders SET payment_method = CASE payment_method
        WHEN '0' THEN 'BANK'
        WHEN '1' THEN 'COD'
        ELSE payment_method
    END;
END;


-- 2. XỬ LÝ BẢNG STOCK_TRANSACTIONS (type) - dự phòng nếu môi trường khác đang là smallint
DECLARE @sqlStock NVARCHAR(MAX) = N'';
SELECT @sqlStock += N'ALTER TABLE stock_transactions DROP CONSTRAINT ' + QUOTENAME(con.name) + N';'
FROM sys.check_constraints con
WHERE con.parent_object_id = OBJECT_ID('stock_transactions')
  AND con.parent_column_id IN (
      SELECT column_id FROM sys.columns 
      WHERE object_id = OBJECT_ID('stock_transactions') AND name = 'type'
  );

IF LEN(@sqlStock) > 0
    EXEC sp_executesql @sqlStock;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'stock_transactions' AND COLUMN_NAME = 'type' 
      AND DATA_TYPE IN ('smallint', 'tinyint', 'int')
)
BEGIN
    ALTER TABLE stock_transactions ALTER COLUMN type VARCHAR(30) NOT NULL;

    UPDATE stock_transactions SET type = CASE type
        WHEN '0' THEN 'IMPORT'
        WHEN '1' THEN 'EXPORT'
        WHEN '2' THEN 'ADJUST'
        WHEN '3' THEN 'SALE'
        WHEN '4' THEN 'CANCEL'
        ELSE type
    END;
END;


-- 3. XỬ LÝ BẢNG ACTIVITY_LOGS (event_type, target_type) - dự phòng nếu môi trường khác đang là smallint
DECLARE @sqlLog NVARCHAR(MAX) = N'';
SELECT @sqlLog += N'ALTER TABLE activity_logs DROP CONSTRAINT ' + QUOTENAME(con.name) + N';'
FROM sys.check_constraints con
WHERE con.parent_object_id = OBJECT_ID('activity_logs')
  AND con.parent_column_id IN (
      SELECT column_id FROM sys.columns 
      WHERE object_id = OBJECT_ID('activity_logs') AND name IN ('event_type', 'target_type')
  );

IF LEN(@sqlLog) > 0
    EXEC sp_executesql @sqlLog;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'activity_logs' AND COLUMN_NAME = 'event_type' 
      AND DATA_TYPE IN ('smallint', 'tinyint', 'int')
)
BEGIN
    ALTER TABLE activity_logs ALTER COLUMN event_type VARCHAR(50) NOT NULL;

    UPDATE activity_logs SET event_type = CASE event_type
        WHEN '0' THEN 'LOGIN'
        WHEN '1' THEN 'LOGOUT'
        WHEN '2' THEN 'CREATE'
        WHEN '3' THEN 'UPDATE'
        WHEN '4' THEN 'DELETE'
        WHEN '5' THEN 'EXPORT'
        ELSE event_type
    END;
END;

IF EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'activity_logs' AND COLUMN_NAME = 'target_type' 
      AND DATA_TYPE IN ('smallint', 'tinyint', 'int')
)
BEGIN
    ALTER TABLE activity_logs ALTER COLUMN target_type VARCHAR(50) NULL;

    UPDATE activity_logs SET target_type = CASE target_type
        WHEN '0' THEN 'ORDER'
        WHEN '1' THEN 'PRODUCT'
        WHEN '2' THEN 'CATEGORY'
        WHEN '3' THEN 'USER'
        WHEN '4' THEN 'STOCK'
        WHEN '5' THEN 'REVIEW'
        ELSE target_type
    END;
END;

