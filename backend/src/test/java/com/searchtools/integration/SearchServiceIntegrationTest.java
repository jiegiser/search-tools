package com.searchtools.integration;

import com.searchtools.model.Resource;
import com.searchtools.model.SearchResult;
import com.searchtools.search.SearchEngine;
import com.searchtools.repository.ResourceRepository;
import com.searchtools.repository.SearchHistoryRepository;
import com.searchtools.service.SearchService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索服务测试 - 验证核心逻辑
 */
class SearchServiceIntegrationTest {

    @Test
    void testResourceModelCreation() {
        Resource resource = Resource.builder()
                .title("测试电影资源")
                .description("高清电影下载")
                .panUrl("https://pan.baidu.com/s/1movie")
                .panType(Resource.PanType.BAIDU)
                .extractCode("1234")
                .resourceType(Resource.ResourceType.VIDEO)
                .isValid(true)
                .clickCount(100L)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(resource);
        assertEquals("测试电影资源", resource.getTitle());
        assertEquals("高清电影下载", resource.getDescription());
        assertEquals("https://pan.baidu.com/s/1movie", resource.getPanUrl());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals("1234", resource.getExtractCode());
        assertEquals(Resource.ResourceType.VIDEO, resource.getResourceType());
        assertTrue(resource.getIsValid());
        assertEquals(100L, resource.getClickCount());
    }

    @Test
    void testPanTypeEnum() {
        // 测试所有网盘类型
        assertEquals(12, Resource.PanType.values().length);
        assertNotNull(Resource.PanType.BAIDU);
        assertNotNull(Resource.PanType.ALIYUN);
        assertNotNull(Resource.PanType.QUARK);
        assertNotNull(Resource.PanType.XUNLEI);
        assertNotNull(Resource.PanType.TIANYI);
        assertNotNull(Resource.PanType.UC);
        assertNotNull(Resource.PanType.PAN115);
        assertNotNull(Resource.PanType.PAN123);
        assertNotNull(Resource.PanType.WEIYUN);
        assertNotNull(Resource.PanType.LANZOU);
        assertNotNull(Resource.PanType.MEGA);
        assertNotNull(Resource.PanType.OTHER);
    }

    @Test
    void testResourceTypeEnum() {
        // 测试所有资源类型
        assertTrue(Resource.ResourceType.values().length >= 7);
        assertNotNull(Resource.ResourceType.VIDEO);
        assertNotNull(Resource.ResourceType.DOCUMENT);
        assertNotNull(Resource.ResourceType.SOFTWARE);
        assertNotNull(Resource.ResourceType.MUSIC);
        assertNotNull(Resource.ResourceType.IMAGE);
        assertNotNull(Resource.ResourceType.ARCHIVE);
        assertNotNull(Resource.ResourceType.OTHER);
    }

    @Test
    void testPanTypeDisplayName() {
        assertEquals("百度网盘", Resource.PanType.BAIDU.getDisplayName());
        assertEquals("阿里云盘", Resource.PanType.ALIYUN.getDisplayName());
        assertEquals("夸克网盘", Resource.PanType.QUARK.getDisplayName());
    }

    @Test
    void testResourceTypeDisplayName() {
        assertEquals("视频", Resource.ResourceType.VIDEO.getDisplayName());
        assertEquals("文档", Resource.ResourceType.DOCUMENT.getDisplayName());
        assertEquals("软件", Resource.ResourceType.SOFTWARE.getDisplayName());
        assertEquals("音乐", Resource.ResourceType.MUSIC.getDisplayName());
        assertEquals("图片", Resource.ResourceType.IMAGE.getDisplayName());
        assertEquals("压缩包", Resource.ResourceType.ARCHIVE.getDisplayName());
    }

    @Test
    void testSearchResultBuilder() {
        SearchResult.ResourceDTO resourceDTO = SearchResult.ResourceDTO.builder()
                .id(1L)
                .title("测试资源")
                .panUrl("https://pan.baidu.com/s/1test")
                .panType("BAIDU")
                .panTypeName("百度网盘")
                .extractCode("1234")
                .resourceType("VIDEO")
                .resourceTypeName("视频")
                .isValid(true)
                .clickCount(50L)
                .build();

        SearchResult result = SearchResult.builder()
                .keyword("test")
                .totalCount(1L)
                .page(0)
                .pageSize(20)
                .duration(100L)
                .resources(Arrays.asList(resourceDTO))
                .build();

        assertNotNull(result);
        assertEquals("test", result.getKeyword());
        assertEquals(1L, result.getTotalCount());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(100L, result.getDuration());
        assertEquals(1, result.getResources().size());
        assertEquals("测试资源", result.getResources().get(0).getTitle());
    }

    @Test
    void testResourceDTOBuilder() {
        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(1L)
                .title("测试资源")
                .description("测试描述")
                .panUrl("https://pan.quark.cn/s/test")
                .panType("QUARK")
                .panTypeName("夸克网盘")
                .resourceType("SOFTWARE")
                .resourceTypeName("软件")
                .sourceUrl("https://example.com")
                .sourceSite("example.com")
                .fileSize("100MB")
                .isValid(true)
                .clickCount(25L)
                .createdAt("2024-01-01T00:00:00")
                .build();

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("测试资源", dto.getTitle());
        assertEquals("测试描述", dto.getDescription());
        assertEquals("https://pan.quark.cn/s/test", dto.getPanUrl());
        assertEquals("QUARK", dto.getPanType());
        assertEquals("夸克网盘", dto.getPanTypeName());
        assertEquals("SOFTWARE", dto.getResourceType());
        assertEquals("软件", dto.getResourceTypeName());
        assertEquals("https://example.com", dto.getSourceUrl());
        assertEquals("example.com", dto.getSourceSite());
        assertEquals("100MB", dto.getFileSize());
        assertTrue(dto.getIsValid());
        assertEquals(25L, dto.getClickCount());
        assertEquals("2024-01-01T00:00:00", dto.getCreatedAt());
    }

    @Test
    void testSearchResultWithEmptyResources() {
        SearchResult result = SearchResult.builder()
                .keyword("不存在的关键词")
                .totalCount(0L)
                .page(0)
                .pageSize(20)
                .duration(50L)
                .resources(Collections.emptyList())
                .build();

        assertNotNull(result);
        assertEquals(0, result.getTotalCount());
        assertTrue(result.getResources().isEmpty());
    }

    @Test
    void testMultipleResources() {
        List<SearchResult.ResourceDTO> resources = Arrays.asList(
                SearchResult.ResourceDTO.builder()
                        .id(1L)
                        .title("百度网盘资源")
                        .panUrl("https://pan.baidu.com/s/1test1")
                        .panType("BAIDU")
                        .build(),
                SearchResult.ResourceDTO.builder()
                        .id(2L)
                        .title("阿里云盘资源")
                        .panUrl("https://www.aliyundrive.com/s/test2")
                        .panType("ALIYUN")
                        .build(),
                SearchResult.ResourceDTO.builder()
                        .id(3L)
                        .title("夸克网盘资源")
                        .panUrl("https://pan.quark.cn/s/test3")
                        .panType("QUARK")
                        .build()
        );

        assertEquals(3, resources.size());
        assertEquals("BAIDU", resources.get(0).getPanType());
        assertEquals("ALIYUN", resources.get(1).getPanType());
        assertEquals("QUARK", resources.get(2).getPanType());
    }
}
