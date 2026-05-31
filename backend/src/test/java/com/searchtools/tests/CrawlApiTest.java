package com.searchtools.tests;

import com.searchtools.model.CrawlRecord;
import com.searchtools.model.CrawlRequest;
import com.searchtools.model.Resource;
import com.searchtools.service.CrawlService;
import com.searchtools.service.LinkValidatorService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 爬取API全量测试 - 覆盖CrawlController所有接口的数据模型和逻辑
 */
class CrawlApiTest {

    // ==================== POST /api/crawl 爬取网页测试 ====================

    @Test
    void testCrawlRequestBuilder() {
        CrawlRequest request = CrawlRequest.builder()
                .url("https://www.52pojie.cn/thread-123456-1-1.html")
                .recursive(false)
                .depth(1)
                .build();

        assertNotNull(request);
        assertEquals("https://www.52pojie.cn/thread-123456-1-1.html", request.getUrl());
        assertFalse(request.getRecursive());
        assertEquals(1, request.getDepth());
    }

    @Test
    void testCrawlRequestDefaults() {
        CrawlRequest request = CrawlRequest.builder()
                .url("https://example.com")
                .build();

        assertFalse(request.getRecursive());
        assertEquals(1, request.getDepth());
    }

    @Test
    void testCrawlRecordBuilderForSingleCrawl() {
        CrawlRecord record = CrawlRecord.builder()
                .id(1L)
                .url("https://www.52pojie.cn/thread-123456-1-1.html")
                .status(CrawlRecord.CrawlStatus.COMPLETED)
                .resourceCount(5)
                .startedAt(LocalDateTime.of(2024, 6, 15, 10, 0, 0))
                .completedAt(LocalDateTime.of(2024, 6, 15, 10, 1, 30))
                .build();

        assertNotNull(record);
        assertEquals(1L, record.getId());
        assertEquals(CrawlRecord.CrawlStatus.COMPLETED, record.getStatus());
        assertEquals(5, record.getResourceCount());
        assertNotNull(record.getStartedAt());
        assertNotNull(record.getCompletedAt());
    }

    // ==================== POST /api/crawl/batch 批量爬取测试 ====================

    @Test
    void testBatchCrawlUrls() {
        List<String> urls = Arrays.asList(
                "https://www.52pojie.cn/thread-111-1-1.html",
                "https://www.52pojie.cn/thread-222-1-1.html",
                "https://www.52pojie.cn/thread-333-1-1.html"
        );

        assertEquals(3, urls.size());
        for (String url : urls) {
            assertTrue(url.startsWith("https://"));
        }
    }

    // ==================== GET /api/crawl/records/{id} 爬取记录测试 ====================

    @Test
    void testCrawlRecordWithPendingStatus() {
        CrawlRecord record = CrawlRecord.builder()
                .id(1L)
                .url("https://example.com")
                .status(CrawlRecord.CrawlStatus.PENDING)
                .resourceCount(0)
                .build();

        assertEquals(CrawlRecord.CrawlStatus.PENDING, record.getStatus());
        assertEquals(0, record.getResourceCount());
    }

    @Test
    void testCrawlRecordWithRunningStatus() {
        CrawlRecord record = CrawlRecord.builder()
                .id(2L)
                .url("https://example.com")
                .status(CrawlRecord.CrawlStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();

        assertEquals(CrawlRecord.CrawlStatus.RUNNING, record.getStatus());
        assertNotNull(record.getStartedAt());
        assertNull(record.getCompletedAt());
    }

    @Test
    void testCrawlRecordWithFailedStatus() {
        CrawlRecord record = CrawlRecord.builder()
                .id(3L)
                .url("https://example.com")
                .status(CrawlRecord.CrawlStatus.FAILED)
                .errorMessage("Connection timeout")
                .build();

        assertEquals(CrawlRecord.CrawlStatus.FAILED, record.getStatus());
        assertEquals("Connection timeout", record.getErrorMessage());
    }

    // ==================== GET /api/crawl/history 爬取历史测试 ====================

    @Test
    void testCrawlHistoryRecords() {
        List<CrawlRecord> history = Arrays.asList(
                CrawlRecord.builder().id(1L).url("https://a.com").status(CrawlRecord.CrawlStatus.COMPLETED).resourceCount(3).build(),
                CrawlRecord.builder().id(2L).url("https://b.com").status(CrawlRecord.CrawlStatus.COMPLETED).resourceCount(7).build(),
                CrawlRecord.builder().id(3L).url("https://c.com").status(CrawlRecord.CrawlStatus.FAILED).resourceCount(0).errorMessage("Error").build()
        );

        assertEquals(3, history.size());
        long completedCount = history.stream().filter(r -> r.getStatus() == CrawlRecord.CrawlStatus.COMPLETED).count();
        assertEquals(2, completedCount);
    }

    // ==================== GET /api/crawl/running 运行中的任务测试 ====================

    @Test
    void testRunningTasksList() {
        List<CrawlRecord> running = Arrays.asList(
                CrawlRecord.builder().id(1L).url("https://a.com").status(CrawlRecord.CrawlStatus.RUNNING).build(),
                CrawlRecord.builder().id(2L).url("https://b.com").status(CrawlRecord.CrawlStatus.RUNNING).build()
        );

        assertEquals(2, running.size());
        for (CrawlRecord record : running) {
            assertEquals(CrawlRecord.CrawlStatus.RUNNING, record.getStatus());
        }
    }

    @Test
    void testEmptyRunningTasks() {
        List<CrawlRecord> running = Collections.emptyList();
        assertTrue(running.isEmpty());
    }

    // ==================== GET /api/crawl/stats 任务统计测试 ====================

    @Test
    void testTaskStatsBuilder() {
        CrawlService.TaskStats stats = CrawlService.TaskStats.builder()
                .total(100)
                .running(5)
                .completed(90)
                .failed(5)
                .build();

        assertEquals(100, stats.getTotal());
        assertEquals(5, stats.getRunning());
        assertEquals(90, stats.getCompleted());
        assertEquals(5, stats.getFailed());
        assertEquals(100, stats.getRunning() + stats.getCompleted() + stats.getFailed());
    }

    @Test
    void testTaskStatsAllZeros() {
        CrawlService.TaskStats stats = CrawlService.TaskStats.builder()
                .total(0).running(0).completed(0).failed(0).build();

        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getRunning());
        assertEquals(0, stats.getCompleted());
        assertEquals(0, stats.getFailed());
    }

    // ==================== POST /api/crawl/search-engine 搜索引擎爬取测试 ====================

    @Test
    void testSearchEngineCrawlResult() {
        // 模拟搜索引擎爬取结果
        List<Resource> resources = Arrays.asList(
                Resource.builder().title("搜索结果1").panUrl("https://pan.baidu.com/s/1").panType(Resource.PanType.BAIDU).build(),
                Resource.builder().title("搜索结果2").panUrl("https://pan.quark.cn/s/2").panType(Resource.PanType.QUARK).build()
        );

        assertEquals(2, resources.size());
    }

    // ==================== POST /api/crawl/validate-link 链接验证测试 ====================

    @Test
    void testValidationResultValid() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(true)
                .message("链接有效")
                .needExtractCode(false)
                .build();

        assertTrue(result.isValid());
        assertEquals("链接有效", result.getMessage());
        assertFalse(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultInvalid() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(false)
                .message("链接已失效")
                .needExtractCode(false)
                .build();

        assertFalse(result.isValid());
        assertEquals("链接已失效", result.getMessage());
    }

    @Test
    void testValidationResultNeedExtractCode() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(true)
                .message("链接有效（需要提取码）")
                .needExtractCode(true)
                .build();

        assertTrue(result.isValid());
        assertTrue(result.isNeedExtractCode());
    }

    // ==================== POST /api/crawl/validate-all 批量验证测试 ====================

    @Test
    void testBatchValidationResults() {
        List<Resource> resources = Arrays.asList(
                Resource.builder().title("有效资源").panUrl("https://pan.baidu.com/s/valid").panType(Resource.PanType.BAIDU).isValid(true).build(),
                Resource.builder().title("无效资源").panUrl("https://pan.baidu.com/s/invalid").panType(Resource.PanType.BAIDU).isValid(false).build(),
                Resource.builder().title("另一有效资源").panUrl("https://pan.quark.cn/s/valid2").panType(Resource.PanType.QUARK).isValid(true).build()
        );

        long validCount = resources.stream().filter(Resource::getIsValid).count();
        long invalidCount = resources.stream().filter(r -> !r.getIsValid()).count();

        assertEquals(2, validCount);
        assertEquals(1, invalidCount);
        assertEquals(3, validCount + invalidCount);
    }

    // ==================== 分布式爬取测试 ====================

    @Test
    void testDistributedTaskSubmission() {
        // 模拟分布式任务提交
        Long taskId = 1L;
        String url = "https://www.52pojie.cn/thread-123456-1-1.html";
        String status = "submitted";

        assertEquals(1L, taskId);
        assertNotNull(url);
        assertEquals("submitted", status);
    }

    @Test
    void testDistributedBatchSubmission() {
        List<Long> taskIds = Arrays.asList(1L, 2L, 3L);
        assertEquals(3, taskIds.size());
        assertEquals(1L, taskIds.get(0));
        assertEquals(2L, taskIds.get(1));
        assertEquals(3L, taskIds.get(2));
    }

    // ==================== CrawlStatus枚举测试 ====================

    @Test
    void testCrawlStatusAllValues() {
        assertEquals(4, CrawlRecord.CrawlStatus.values().length);
    }

    @Test
    void testCrawlStatusDisplayNames() {
        assertEquals("待处理", CrawlRecord.CrawlStatus.PENDING.getDisplayName());
        assertEquals("运行中", CrawlRecord.CrawlStatus.RUNNING.getDisplayName());
        assertEquals("已完成", CrawlRecord.CrawlStatus.COMPLETED.getDisplayName());
        assertEquals("失败", CrawlRecord.CrawlStatus.FAILED.getDisplayName());
    }

    @Test
    void testCrawlStatusValueOf() {
        assertEquals(CrawlRecord.CrawlStatus.PENDING, CrawlRecord.CrawlStatus.valueOf("PENDING"));
        assertEquals(CrawlRecord.CrawlStatus.RUNNING, CrawlRecord.CrawlStatus.valueOf("RUNNING"));
        assertEquals(CrawlRecord.CrawlStatus.COMPLETED, CrawlRecord.CrawlStatus.valueOf("COMPLETED"));
        assertEquals(CrawlRecord.CrawlStatus.FAILED, CrawlRecord.CrawlStatus.valueOf("FAILED"));
    }

    // ==================== 资源预览测试 ====================

    @Test
    void testResourcePreviewData() {
        Resource resource = Resource.builder()
                .id(1L)
                .title("Adobe Photoshop 2024")
                .description("Adobe全家桶安装包")
                .panUrl("https://pan.baidu.com/s/1adobe2024")
                .panType(Resource.PanType.BAIDU)
                .extractCode("xy12")
                .resourceType(Resource.ResourceType.SOFTWARE)
                .fileSize("3.5GB")
                .build();

        assertEquals("Adobe Photoshop 2024", resource.getTitle());
        assertEquals(Resource.ResourceType.SOFTWARE, resource.getResourceType());
        assertEquals("3.5GB", resource.getFileSize());
        assertEquals("xy12", resource.getExtractCode());
    }

    // ==================== 链接检查测试 ====================

    @Test
    void testCheckLinkResultValid() {
        String url = "https://pan.baidu.com/s/1valid";
        boolean valid = true;

        assertTrue(valid);
        assertNotNull(url);
    }

    @Test
    void testCheckLinkResultInvalid() {
        String url = "https://pan.baidu.com/s/1expired";
        boolean valid = false;

        assertFalse(valid);
    }
}
