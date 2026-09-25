-- =========================================================================================
-- Flyway Migration V7: Bổ sung index cho các bảng nghiệp vụ
-- SQL Server KHÔNG tự tạo index cho khóa ngoại, nên các FK + cột lọc/sắp xếp chính
-- (created_at, status, type, quantity...) được index tại đây.
-- Tên index trùng với @Index khai báo trong entity để Hibernate (ddl-auto: update) không tạo lặp.
-- Mỗi lệnh đều kiểm tra bảng tồn tại (DB mới: Flyway chạy trước Hibernate tạo bảng)
-- và index chưa tồn tại (migration chạy lại an toàn).
-- =========================================================================================

DECLARE @indexes TABLE (table_name SYSNAME, index_name SYSNAME, columns NVARCHAR(400));

INSERT INTO @indexes (table_name, index_name, columns) VALUES
    -- activity_logs: sort mặc định created_at DESC, lọc khoảng ngày, lọc event_type, join users
    ('activity_logs',           'idx_activity_logs_created_at',            'created_at'),
    ('activity_logs',           'idx_activity_logs_user_id',               'user_id'),
    ('activity_logs',           'idx_activity_logs_event_type_created_at', 'event_type, created_at'),
    -- orders: đơn của tôi (user_id + created_at), admin/dashboard lọc theo created_at, lọc status
    ('orders',                  'idx_orders_user_id_created_at',           'user_id, created_at'),
    ('orders',                  'idx_orders_created_at',                   'created_at'),
    ('orders',                  'idx_orders_status_created_at',            'status, created_at'),
    -- order_lines: join theo order_id, top selling / review theo product_id, existsByProduct_Id
    ('order_lines',             'idx_order_lines_order_id',                'order_id'),
    ('order_lines',             'idx_order_lines_product_id',              'product_id'),
    -- products: lọc theo danh mục, low-stock/out-of-stock, sort mặc định theo rating
    ('products',                'idx_products_category_id',                'category_id'),
    ('products',                'idx_products_quantity',                   'quantity'),
    ('products',                'idx_products_rating',                     'average_rating, total_reviews, quantity'),
    -- reviews: order_line_id đã được uk_order_user (order_line_id, user_id) bao phủ
    ('reviews',                 'idx_reviews_user_id',                     'user_id'),
    -- stock_transactions: sort mặc định created_at DESC, lọc type, join người tạo
    ('stock_transactions',      'idx_stock_transactions_created_at',       'created_at'),
    ('stock_transactions',      'idx_stock_transactions_type_created_at',  'type, created_at'),
    ('stock_transactions',      'idx_stock_transactions_created_by',       'created_by'),
    -- stock_transaction_lines: lấy dòng theo phiếu, EXISTS lọc theo product_id
    ('stock_transaction_lines', 'idx_stock_tx_lines_transaction_id',       'stock_transaction_id'),
    ('stock_transaction_lines', 'idx_stock_tx_lines_product_id',           'product_id, stock_transaction_id');

DECLARE @sqlIndexes NVARCHAR(MAX) = N'';

SELECT @sqlIndexes += N'CREATE NONCLUSTERED INDEX ' + QUOTENAME(i.index_name)
                    + N' ON dbo.' + QUOTENAME(i.table_name) + N' (' + i.columns + N');' + CHAR(10)
FROM @indexes i
WHERE OBJECT_ID(N'dbo.' + i.table_name, N'U') IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys.indexes si
      WHERE si.object_id = OBJECT_ID(N'dbo.' + i.table_name, N'U')
        AND si.name = i.index_name
  );

IF LEN(@sqlIndexes) > 0
    EXEC sp_executesql @sqlIndexes;


-- -----------------------------------------------------------------------------------------
-- activity_logs.event_id bị trùng unique constraint: uk_activity_event_id (khai báo trong
-- @Table) và một UK tự sinh tên từ @Column(unique = true) cũ. Giữ uk_activity_event_id,
-- gỡ các unique constraint khác chỉ gồm đúng cột event_id.
-- -----------------------------------------------------------------------------------------
DECLARE @sqlDupUk NVARCHAR(MAX) = N'';

SELECT @sqlDupUk += N'ALTER TABLE dbo.activity_logs DROP CONSTRAINT ' + QUOTENAME(kc.name) + N';'
FROM sys.key_constraints kc
WHERE kc.parent_object_id = OBJECT_ID(N'dbo.activity_logs', N'U')
  AND kc.type = 'UQ'
  AND kc.name <> 'uk_activity_event_id'
  AND EXISTS (SELECT 1 FROM sys.key_constraints k2
              WHERE k2.parent_object_id = kc.parent_object_id AND k2.name = 'uk_activity_event_id')
  AND (SELECT COUNT(*) FROM sys.index_columns ic
       WHERE ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id) = 1
  AND EXISTS (
      SELECT 1 FROM sys.index_columns ic
      JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
      WHERE ic.object_id = kc.parent_object_id
        AND ic.index_id = kc.unique_index_id
        AND c.name = 'event_id'
  );

IF LEN(@sqlDupUk) > 0
    EXEC sp_executesql @sqlDupUk;
