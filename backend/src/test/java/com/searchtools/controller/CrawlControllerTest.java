package com.searchtools.controller;

import com.searchtools.crawler.SearchEngineCrawler;
import com.searchtools.model.CrawlRecord;
import com.searchtools.model.CrawlRequest;
import com.searchtools.model.Resource;
import com.searchtools.service.CrawlService;
import com.searchtools.service.LinkValidatorService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 爬取控制器测试 - 纯JUnit测试，验证模型和逻辑
 */
class CrawlControllerTest {

    @Test
    void testCrawlRecordBuilder() {
        CrawlRecord record = CrawlRecord.builder()
                .id(1L)
                .url("https://example.com")
                .status(CrawlRecord.CrawlStatus.COMPLETED)
                .resourceCount(2)
                .build();

        assertNotNull(record);
        assertEquals(1L, record.getId());
        assertEquals("https://example.com", record.getUrl());
        assertEquals(CrawlRecord.CrawlStatus.COMPLETED, record.getStatus());
        assertEquals(2, record.getResourceCount());
    }

    @Test
    void testCrawlRecordStatusValues() {
        assertEquals(4, CrawlRecord.CrawlStatus.values().length);
        assertNotNull(CrawlRecord.CrawlStatus.PENDING);
        assertNotNull(CrawlRecord.CrawlStatus.RUNNING);
        assertNotNull(CrawlRecord.CrawlStatus.COMPLETED);
        assertNotNull(CrawlRecord.CrawlStatus.FAILED);
    }

    @Test
    void testCrawlRequestBuilder() {
        CrawlRequest request = CrawlRequest.builder()
                .url("https://example.com")
                .build();

        assertNotNull(request);
        assertEquals("https://example.com", request.getUrl());
    }

    @Test
    void testTaskStatsBuilder() {
        CrawlService.TaskStats stats = CrawlService.TaskStats.builder()
                .total(10)
                .running(2)
                .completed(7)
                .failed(1)
                .build();

        assertNotNull(stats);
        assertEquals(10, stats.getTotal());
        assertEquals(2, stats.getRunning());
        assertEquals(7, stats.getCompleted());
        assertEquals(1, stats.getFailed());
    }

    @Test
    void testResourceBuilder() {
        Resource resource = Resource.builder()
                .id(1L)
                .title("测试资源")
                .panUrl("https://pan.baidu.com/s/1test")
                .panType(Resource.PanType.BAIDU)
                .extractCode("1234")
                .resourceType(Resource.ResourceType.VIDEO)
                .isValid(true)
                .clickCount(50L)
                .build();

        assertNotNull(resource);
        assertEquals(1L, resource.getId());
        assertEquals("测试资源", resource.getTitle());
        assertEquals("https://pan.baidu.com/s/1test", resource.getPanUrl());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals("1234", resource.getExtractCode());
        assertEquals(Resource.ResourceType.VIDEO, resource.getResourceType());
        assertTrue(resource.getIsValid());
        assertEquals(50L, resource.getClickCount());
    }

    @Test
    void testPanTypeEnum() {
        assertEquals(12, Resource.PanType.values().length);
        assertEquals("百度网盘", Resource.PanType.BAIDU.getDisplayName());
        assertEquals("阿里云盘", Resource.PanType.ALIYUN.getDisplayName());
        assertEquals("夸克网盘", Resource.PanType.QUARK.getDisplayName());
        assertEquals("迅雷网盘", Resource.PanType.XUNLEI.getDisplayName());
        assertEquals("天翼云盘", Resource.PanType.TIANYI.getDisplayName());
        assertEquals("UC网盘", Resource.PanType.UC.getDisplayName());
        assertEquals("115网盘", Resource.PanType.PAN115.getDisplayName());
        assertEquals("123网盘", Resource.PanType.PAN123.getDisplayName());
        assertEquals("微云", Resource.PanType.WEIYUN.getDisplayName());
        assertEquals("蓝奏云", Resource.PanType.LANZOU.getDisplayName());
        assertEquals("MEGA", Resource.PanType.MEGA.getDisplayName());
        assertEquals("其他", Resource.PanType.OTHER.getDisplayName());
    }

    @Test
    void testResourceTypeEnum() {
        assertTrue(Resource.ResourceType.values().length >= 7);
        assertEquals("视频", Resource.ResourceType.VIDEO.getDisplayName());
        assertEquals("文档", Resource.ResourceType.DOCUMENT.getDisplayName());
        assertEquals("软件", Resource.ResourceType.SOFTWARE.getDisplayName());
        assertEquals("音乐", Resource.ResourceType.MUSIC.getDisplayName());
    }

    @Test
    void testValidationResultBuilder() {
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
}
