-- =========================================================================================
-- Flyway Migration V3: Stored Procedure tổng hợp số liệu Dashboard (sp_GetDashboardOverview)
-- =========================================================================================
CREATE OR ALTER PROCEDURE dbo.sp_GetDashboardOverview
    @rangeDays INT = 30,
    @fromDate DATETIME2 = NULL,
    @toDate   DATETIME2 = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Nếu không truyền fromDate, toDate thì tính theo @rangeDays gần nhất
    IF @fromDate IS NULL
        SET @fromDate = DATEADD(DAY, -@rangeDays, CAST(CONVERT(DATE, SYSUTCDATETIME()) AS DATETIME2));
    IF @toDate IS NULL
        SET @toDate = SYSUTCDATETIME();

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

    -- RESULT SET 2: DOANH THU THEO TỪNG NGÀY (RECURSIVE CTE ĐỂ NGÀY KHÔNG CÓ ĐƠN VẪN HIỂN THỊ DOANH THU 0)
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

