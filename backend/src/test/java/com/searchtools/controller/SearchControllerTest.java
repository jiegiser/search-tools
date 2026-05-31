package com.searchtools.controller;

import com.searchtools.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索控制器测试 - 纯JUnit测试，验证模型和逻辑
 */
class SearchControllerTest {

    @Test
    void testSearchResultBuilder() {
        SearchResult result = SearchResult.builder()
                .keyword("test")
                .totalCount(1L)
                .page(0)
                .pageSize(20)
                .duration(100L)
                .resources(Arrays.asList(
                        SearchResult.ResourceDTO.builder()
                                .id(1L)
                                .title("Test Resource")
                                .panUrl("https://pan.baidu.com/s/1test")
                                .panType("BAIDU")
                                .panTypeName("百度网盘")
                                .build()
                ))
                .build();

        assertNotNull(result);
        assertEquals("test", result.getKeyword());
        assertEquals(1L, result.getTotalCount());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(100L, result.getDuration());
        assertEquals(1, result.getResources().size());
        assertEquals("Test Resource", result.getResources().get(0).getTitle());
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
    void testResourceDTOBuilder() {
        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(1L)
                .title("测试资源")
                .description("测试描述")
                .panUrl("https://pan.baidu.com/s/test")
                .panType("BAIDU")
                .panTypeName("百度网盘")
                .extractCode("1234")
                .resourceType("VIDEO")
                .resourceTypeName("视频")
                .sourceUrl("https://example.com")
                .sourceSite("example.com")
                .fileSize("100MB")
                .isValid(true)
                .clickCount(50L)
                .createdAt("2024-01-01T00:00:00")
                .build();

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("测试资源", dto.getTitle());
        assertEquals("测试描述", dto.getDescription());
        assertEquals("https://pan.baidu.com/s/test", dto.getPanUrl());
        assertEquals("BAIDU", dto.getPanType());
        assertEquals("百度网盘", dto.getPanTypeName());
        assertEquals("1234", dto.getExtractCode());
        assertEquals("VIDEO", dto.getResourceType());
        assertEquals("视频", dto.getResourceTypeName());
        assertEquals("https://example.com", dto.getSourceUrl());
        assertEquals("example.com", dto.getSourceSite());
        assertEquals("100MB", dto.getFileSize());
        assertTrue(dto.getIsValid());
        assertEquals(50L, dto.getClickCount());
        assertEquals("2024-01-01T00:00:00", dto.getCreatedAt());
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

        SearchResult result = SearchResult.builder()
                .keyword("网盘资源")
                .totalCount(3L)
                .page(0)
                .pageSize(20)
                .duration(150L)
                .resources(resources)
                .build();

        assertEquals(3, result.getResources().size());
        assertEquals("BAIDU", result.getResources().get(0).getPanType());
        assertEquals("ALIYUN", result.getResources().get(1).getPanType());
        assertEquals("QUARK", result.getResources().get(2).getPanType());
    }

    @Test
    void testPagination() {
        SearchResult result = SearchResult.builder()
                .keyword("test")
                .totalCount(100L)
                .page(2)
                .pageSize(20)
                .duration(80L)
                .resources(Collections.emptyList())
                .build();

        assertEquals(2, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(100L, result.getTotalCount());
    }

    @Test
    void testResourceDTOWithNullFields() {
        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(1L)
                .title("测试资源")
                .panUrl("https://pan.quark.cn/s/test")
                .panType("QUARK")
                .panTypeName("夸克网盘")
                .build();

        assertNotNull(dto);
        assertNull(dto.getDescription());
        assertNull(dto.getExtractCode());
        assertNull(dto.getResourceType());
        assertNull(dto.getSourceUrl());
        assertNull(dto.getFileSize());
    }
}
