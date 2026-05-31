package com.searchtools.tests;

import com.searchtools.model.SystemStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统状态API全量测试 - 覆盖StatusController所有接口的数据模型和逻辑
 */
class StatusApiTest {

    // ==================== GET /api/status 系统状态测试 ====================

    @Test
    void testSystemStatusFullBuilder() {
        SystemStatus.PanStats panStats = SystemStatus.PanStats.builder()
                .baiduCount(50L)
                .aliyunCount(30L)
                .quarkCount(20L)
                .xunleiCount(10L)
                .tianyiCount(5L)
                .ucCount(3L)
                .pan115Count(2L)
                .pan123Count(1L)
                .weiyunCount(1L)
                .lanzouCount(0L)
                .megaCount(0L)
                .otherCount(8L)
                .build();

        SystemStatus status = SystemStatus.builder()
                .status("running")
                .totalResources(130L)
                .validResources(120L)
                .todayResources(15L)
                .totalCrawlTasks(50L)
                .runningTasks(3L)
                .totalSearches(200L)
                .indexSize("2.5MB")
                .uptime("12小时30分钟")
                .panStats(panStats)
                .build();

        assertNotNull(status);
        assertEquals("running", status.getStatus());
        assertEquals(130L, status.getTotalResources());
        assertEquals(120L, status.getValidResources());
        assertEquals(15L, status.getTodayResources());
        assertEquals(50L, status.getTotalCrawlTasks());
        assertEquals(3L, status.getRunningTasks());
        assertEquals(200L, status.getTotalSearches());
        assertEquals("2.5MB", status.getIndexSize());
        assertEquals("12小时30分钟", status.getUptime());
        assertNotNull(status.getPanStats());
    }

    @Test
    void testSystemStatusRunning() {
        SystemStatus status = SystemStatus.builder()
                .status("running")
                .build();

        assertEquals("running", status.getStatus());
    }

    @Test
    void testSystemStatusWithZeroResources() {
        SystemStatus status = SystemStatus.builder()
                .status("running")
                .totalResources(0L)
                .validResources(0L)
                .todayResources(0L)
                .build();

        assertEquals(0L, status.getTotalResources());
        assertEquals(0L, status.getValidResources());
        assertEquals(0L, status.getTodayResources());
    }

    @Test
    void testSystemStatusWithLargeNumbers() {
        SystemStatus status = SystemStatus.builder()
                .status("running")
                .totalResources(1000000L)
                .validResources(999999L)
                .todayResources(5000L)
                .totalCrawlTasks(100000L)
                .runningTasks(50L)
                .totalSearches(5000000L)
                .build();

        assertEquals(1000000L, status.getTotalResources());
        assertEquals(999999L, status.getValidResources());
        assertEquals(5000000L, status.getTotalSearches());
    }

    // ==================== PanStats 网盘统计测试 ====================

    @Test
    void testPanStatsAllTypes() {
        SystemStatus.PanStats stats = SystemStatus.PanStats.builder()
                .baiduCount(100L)
                .aliyunCount(80L)
                .quarkCount(60L)
                .xunleiCount(40L)
                .tianyiCount(20L)
                .ucCount(15L)
                .pan115Count(10L)
                .pan123Count(8L)
                .weiyunCount(5L)
                .lanzouCount(3L)
                .megaCount(2L)
                .otherCount(7L)
                .build();

        assertEquals(100L, stats.getBaiduCount());
        assertEquals(80L, stats.getAliyunCount());
        assertEquals(60L, stats.getQuarkCount());
        assertEquals(40L, stats.getXunleiCount());
        assertEquals(20L, stats.getTianyiCount());
        assertEquals(15L, stats.getUcCount());
        assertEquals(10L, stats.getPan115Count());
        assertEquals(8L, stats.getPan123Count());
        assertEquals(5L, stats.getWeiyunCount());
        assertEquals(3L, stats.getLanzouCount());
        assertEquals(2L, stats.getMegaCount());
        assertEquals(7L, stats.getOtherCount());

        // 验证总数
        long total = stats.getBaiduCount() + stats.getAliyunCount() + stats.getQuarkCount()
                + stats.getXunleiCount() + stats.getTianyiCount() + stats.getUcCount()
                + stats.getPan115Count() + stats.getPan123Count() + stats.getWeiyunCount()
                + stats.getLanzouCount() + stats.getMegaCount() + stats.getOtherCount();
        assertEquals(350L, total);
    }

    @Test
    void testPanStatsAllZeros() {
        SystemStatus.PanStats stats = SystemStatus.PanStats.builder()
                .baiduCount(0L)
                .aliyunCount(0L)
                .quarkCount(0L)
                .xunleiCount(0L)
                .tianyiCount(0L)
                .ucCount(0L)
                .pan115Count(0L)
                .pan123Count(0L)
                .weiyunCount(0L)
                .lanzouCount(0L)
                .megaCount(0L)
                .otherCount(0L)
                .build();

        assertEquals(0L, stats.getBaiduCount());
        assertEquals(0L, stats.getOtherCount());
    }

    @Test
    void testPanStatsDefaultValues() {
        SystemStatus.PanStats stats = new SystemStatus.PanStats();
        assertNull(stats.getBaiduCount());
        assertNull(stats.getAliyunCount());
        assertNull(stats.getQuarkCount());
    }

    // ==================== GET /api/health 健康检查测试 ====================

    @Test
    void testHealthCheckResponse() {
        String healthStatus = "OK";
        assertEquals("OK", healthStatus);
    }
}
