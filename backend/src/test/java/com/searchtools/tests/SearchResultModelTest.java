package com.searchtools.tests;

import com.searchtools.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchResult模型全面测试
 */
class SearchResultModelTest {

    @Test
    void testSearchResultBuilder() {
        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(1L)
                .title("测试资源")
                .panUrl("https://pan.baidu.com/s/test")
                .panType("BAIDU")
                .panTypeName("百度网盘")
                .extractCode("1234")
                .resourceType("DOCUMENT")
                .resourceTypeName("文档")
                .isValid(true)
                .clickCount(50L)
                .build();

        SearchResult result = SearchResult.builder()
                .keyword("test")
                .totalCount(1L)
                .page(0)
                .pageSize(20)
                .duration(100L)
                .resources(Arrays.asList(dto))
                .build();

        assertNotNull(result);
        assertEquals("test", result.getKeyword());
        assertEquals(1L, result.getTotalCount());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(100L, result.getDuration());
        assertEquals(1, result.getResources().size());
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
                .resourceType("VIDEO")
                .resourceTypeName("视频")
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
        assertEquals("VIDEO", dto.getResourceType());
        assertEquals("视频", dto.getResourceTypeName());
        assertEquals("https://example.com", dto.getSourceUrl());
        assertEquals("example.com", dto.getSourceSite());
        assertEquals("100MB", dto.getFileSize());
        assertTrue(dto.getIsValid());
        assertEquals(25L, dto.getClickCount());
        assertEquals("2024-01-01T00:00:00", dto.getCreatedAt());
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
        assertNull(dto.getCreatedAt());
    }

    @Test
    void testSearchResultWithEmptyResources() {
        SearchResult result = SearchResult.builder()
                .keyword("empty")
                .totalCount(0L)
                .page(0)
                .pageSize(20)
                .duration(50L)
                .resources(Collections.emptyList())
                .build();

        assertNotNull(result);
        assertEquals(0L, result.getTotalCount());
        assertTrue(result.getResources().isEmpty());
    }

    @Test
    void testMultipleResources() {
        List<SearchResult.ResourceDTO> resources = Arrays.asList(
                SearchResult.ResourceDTO.builder().id(1L).title("百度资源").panUrl("https://pan.baidu.com/s/1").panType("BAIDU").build(),
                SearchResult.ResourceDTO.builder().id(2L).title("阿里资源").panUrl("https://www.alipan.com/s/2").panType("ALIYUN").build(),
                SearchResult.ResourceDTO.builder().id(3L).title("夸克资源").panUrl("https://pan.quark.cn/s/3").panType("QUARK").build()
        );

        SearchResult result = SearchResult.builder()
                .keyword("网盘")
                .totalCount(3L)
                .page(0)
                .pageSize(20)
                .duration(100L)
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
    void testResourceDTOEquality() {
        SearchResult.ResourceDTO dto1 = SearchResult.ResourceDTO.builder()
                .id(1L).title("Test").panUrl("https://test.com").panType("BAIDU").build();
        SearchResult.ResourceDTO dto2 = SearchResult.ResourceDTO.builder()
                .id(1L).title("Test").panUrl("https://test.com").panType("BAIDU").build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testResourceDTODefaultValues() {
        SearchResult.ResourceDTO dto = new SearchResult.ResourceDTO();
        assertNull(dto.getId());
        assertNull(dto.getTitle());
        assertNull(dto.getPanUrl());
        assertNull(dto.getIsValid());
        assertNull(dto.getClickCount());
    }
}
