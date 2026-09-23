-- =========================================================================================
-- Flyway Migration V6: Chuẩn hóa Stored Procedure tổng hợp số liệu Dashboard (sp_GetDashboardOverview)
-- Trả về chính xác 6 Result Sets đáp ứng 100% nhu cầu hiển thị của giao diện Dashboard:
--   1. Thống kê KPI tổng quan (Doanh thu, Đơn hàng, Tồn kho, Khách hàng)
--   2. Xu hướng doanh thu & số đơn theo ngày (Recursive CTE)
--   3. Top 5 sản phẩm bán chạy nhất
--   4. Cơ cấu sản phẩm theo danh mục
--   5. Top 5 sản phẩm được đánh giá tốt nhất (Reviews)
--   6. Top 5 nhật ký hoạt động gần nhất (Activity Logs)
-- =========================================================================================

CREATE OR ALTER PROCEDURE dbo.sp_GetDashboardOverview
    @rangeDays INT = 7,
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

    -- -------------------------------------------------------------------------------------
    -- RESULT SET 1: THẺ TỔNG QUAN KPI (KPI Overview Cards)
    -- Bao gồm toàn bộ số liệu đơn hàng theo kỳ lọc, cùng số lượng tổng sản phẩm, tồn kho và người dùng
    -- -------------------------------------------------------------------------------------
    SELECT 
        COUNT(o.id) AS totalOrders,
        COALESCE(SUM(CASE WHEN o.status != 'CANCELLED' THEN o.total_amount ELSE 0 END), 0) AS totalRevenue,
        COALESCE(SUM(CASE WHEN o.status IN ('CREATED', 'CONFIRMED') THEN 1 ELSE 0 END), 0) AS pendingOrders,
        COALESCE(SUM(CASE WHEN o.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completedOrders,
        COALESCE(SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelledOrders,
        COALESCE(SUM(CASE WHEN o.status = 'CREATED' THEN 1 ELSE 0 END), 0) AS createdOrders,
        COALESCE(SUM(CASE WHEN o.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) AS confirmedOrders,
        (SELECT COUNT(*) FROM products) AS totalProducts,
        (SELECT COUNT(*) FROM products WHERE quantity <= 10) AS lowStockProducts,
        (SELECT COUNT(*) FROM products WHERE quantity = 0) AS outOfStockProducts,
        (SELECT COUNT(*) FROM users) AS totalUsers
    FROM orders o
    WHERE o.created_at >= @fromDate AND o.created_at <= @toDate;

    -- -------------------------------------------------------------------------------------
    -- RESULT SET 2: DOANH THU & SỐ ĐƠN THEO TỪNG NGÀY (Daily Revenue Trend)
    -- Sử dụng Recursive CTE để ngày không có đơn vẫn hiển thị mốc ngày với doanh thu 0
    -- -------------------------------------------------------------------------------------
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

    -- -------------------------------------------------------------------------------------
    -- RESULT SET 3: TOP 5 SẢN PHẨM BÁN CHẠY NHẤT (Top Selling Products)
    -- -------------------------------------------------------------------------------------
    SELECT TOP 5
        p.id AS productId,
        p.name AS productName,
        p.image AS productImage,
        p.price AS price,
        COALESCE(c.name, N'—') AS categoryName,
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

    -- -------------------------------------------------------------------------------------
    -- RESULT SET 4: CƠ CẤU SẢN PHẨM THEO DANH MỤC (Category Distribution)
    -- -------------------------------------------------------------------------------------
    SELECT 
        COALESCE(c.name, N'Khác') AS categoryName, 
        COUNT(p.id) AS productCount
    FROM categories c
    LEFT JOIN products p ON p.category_id = c.id
    GROUP BY c.id, c.name
    ORDER BY productCount DESC;

    -- -------------------------------------------------------------------------------------
    -- RESULT SET 5: TOP 5 SẢN PHẨM ĐÁNH GIÁ TỐT NHẤT (Top Rated Products)
    -- -------------------------------------------------------------------------------------
    SELECT TOP 5
        p.id AS productId,
        p.name AS productName,
        p.image AS productImage,
        p.price AS price,
        COALESCE(c.name, N'—') AS categoryName,
        ROUND(AVG(CAST(r.rating AS FLOAT)), 1) AS averageRating,
        COUNT(r.id) AS totalReviews
    FROM reviews r
    INNER JOIN order_lines ol ON r.order_line_id = ol.id
    INNER JOIN products p ON ol.product_id = p.id
    LEFT JOIN categories c ON p.category_id = c.id
    GROUP BY p.id, p.name, p.image, p.price, c.name
    HAVING COUNT(r.id) > 0
    ORDER BY AVG(CAST(r.rating AS FLOAT)) DESC, COUNT(r.id) DESC;

    -- -------------------------------------------------------------------------------------
    -- RESULT SET 6: TOP 5 NHẬT KÝ HOẠT ĐỘNG GẦN NHẤT (Recent Activity Logs)
    -- -------------------------------------------------------------------------------------
    SELECT TOP 5
        al.id,
        al.event_id AS eventId,
        al.user_id AS userId,
        u.username AS username,
        RTRIM(LTRIM(CONCAT(COALESCE(u.first_name, ''), ' ', COALESCE(u.last_name, '')))) AS fullName,
        al.event_type AS eventType,
        al.target_type AS targetType,
        al.target_id AS targetId,
        al.ip_address AS ipAddress,
        al.created_at AS createdAt
    FROM activity_logs al
    LEFT JOIN users u ON al.user_id = u.id
    ORDER BY al.created_at DESC;
END;

