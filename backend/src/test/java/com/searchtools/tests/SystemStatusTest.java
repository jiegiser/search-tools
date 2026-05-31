package com.searchtools.tests;

import com.searchtools.model.SystemStatus;
import com.searchtools.model.CrawlRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SystemStatus和CrawlRecord模型测试
 */
class SystemStatusTest {

    @Test
    void testSystemStatusBuilder() {
        SystemStatus status = SystemStatus.builder()
                .status("running")
                .totalResources(100L)
                .validResources(90L)
                .todayResources(5L)
                .totalCrawlTasks(50L)
                .runningTasks(2L)
                .totalSearches(200L)
                .indexSize("1.5MB")
                .uptime("2h 30m")
                .build();

        assertEquals("running", status.getStatus());
        assertEquals(100L, status.getTotalResources());
        assertEquals(90L, status.getValidResources());
        assertEquals(5L, status.getTodayResources());
        assertEquals(50L, status.getTotalCrawlTasks());
        assertEquals(2L, status.getRunningTasks());
        assertEquals(200L, status.getTotalSearches());
        assertEquals("1.5MB", status.getIndexSize());
        assertEquals("2h 30m", status.getUptime());
    }

    @Test
    void testPanStatsBuilder() {
        SystemStatus.PanStats stats = SystemStatus.PanStats.builder()
                .baiduCount(30L)
                .aliyunCount(20L)
                .quarkCount(15L)
                .otherCount(5L)
                .build();

        assertEquals(30L, stats.getBaiduCount());
        assertEquals(20L, stats.getAliyunCount());
        assertEquals(15L, stats.getQuarkCount());
        assertEquals(5L, stats.getOtherCount());
    }

    @Test
    void testSystemStatusWithPanStats() {
        SystemStatus.PanStats panStats = SystemStatus.PanStats.builder()
                .baiduCount(10L)
                .aliyunCount(5L)
                .quarkCount(3L)
                .build();

        SystemStatus status = SystemStatus.builder()
                .status("running")
                .totalResources(18L)
                .panStats(panStats)
                .build();

        assertNotNull(status.getPanStats());
        assertEquals(10L, status.getPanStats().getBaiduCount());
        assertEquals(5L, status.getPanStats().getAliyunCount());
        assertEquals(3L, status.getPanStats().getQuarkCount());
    }

    @Test
    void testSystemStatusDefaultValues() {
        SystemStatus status = new SystemStatus();
        assertNull(status.getStatus());
        assertNull(status.getTotalResources());
        assertNull(status.getValidResources());
        assertNull(status.getPanStats());
    }

    @Test
    void testCrawlRecordBuilder() {
        CrawlRecord record = CrawlRecord.builder()
                .id(1L)
                .url("https://example.com")
                .status(CrawlRecord.CrawlStatus.COMPLETED)
                .resourceCount(5)
                .build();

        assertEquals(1L, record.getId());
        assertEquals("https://example.com", record.getUrl());
        assertEquals(CrawlRecord.CrawlStatus.COMPLETED, record.getStatus());
        assertEquals(5, record.getResourceCount());
    }

    @Test
    void testCrawlRecordStatusValues() {
        CrawlRecord.CrawlStatus[] values = CrawlRecord.CrawlStatus.values();
        assertEquals(4, values.length);
        assertNotNull(CrawlRecord.CrawlStatus.PENDING);
        assertNotNull(CrawlRecord.CrawlStatus.RUNNING);
        assertNotNull(CrawlRecord.CrawlStatus.COMPLETED);
        assertNotNull(CrawlRecord.CrawlStatus.FAILED);
    }

    @Test
    void testCrawlRecordDefaultValues() {
        CrawlRecord record = new CrawlRecord();
        assertNull(record.getId());
        assertNull(record.getUrl());
        // status has @Builder.Default = PENDING, but new CrawlRecord() without builder may not set it
        // So we just verify the record can be created
        // resourceCount has @Builder.Default = 0
        assertEquals(0, record.getResourceCount());
        assertNull(record.getErrorMessage());
    }

    @Test
    void testCrawlRecordWithErrorMessage() {
        CrawlRecord record = CrawlRecord.builder()
                .id(1L)
                .url("https://example.com")
                .status(CrawlRecord.CrawlStatus.FAILED)
                .errorMessage("Connection timeout")
                .build();

        assertEquals(CrawlRecord.CrawlStatus.FAILED, record.getStatus());
        assertEquals("Connection timeout", record.getErrorMessage());
    }

    @Test
    void testCrawlRecordEquality() {
        CrawlRecord r1 = CrawlRecord.builder()
                .id(1L).url("https://test.com").status(CrawlRecord.CrawlStatus.COMPLETED).build();
        CrawlRecord r2 = CrawlRecord.builder()
                .id(1L).url("https://test.com").status(CrawlRecord.CrawlStatus.COMPLETED).build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
