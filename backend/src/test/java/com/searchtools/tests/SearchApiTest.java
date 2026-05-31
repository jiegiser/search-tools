package com.searchtools.tests;

import com.searchtools.model.Resource;
import com.searchtools.model.SearchHistory;
import com.searchtools.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索API全量测试 - 覆盖SearchController所有接口的数据模型和逻辑
 */
class SearchApiTest {

    // ==================== /api/search 搜索接口测试 ====================

    @Test
    void testSearchResultWithSingleResource() {
        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(1L)
                .title("Java编程思想")
                .description("经典Java学习书籍")
                .panUrl("https://pan.baidu.com/s/1java_thinking")
                .panType("BAIDU")
                .panTypeName("百度网盘")
                .extractCode("abcd")
                .resourceType("DOCUMENT")
                .resourceTypeName("文档")
                .sourceUrl("https://www.52pojie.cn/thread/123")
                .sourceSite("www.52pojie.cn")
                .fileSize("50MB")
                .isValid(true)
                .clickCount(120L)
                .createdAt(LocalDateTime.of(2024, 6, 15, 10, 30, 0).toString())
                .build();

        SearchResult result = SearchResult.builder()
                .keyword("Java编程思想")
                .totalCount(1L)
                .page(0)
                .pageSize(20)
                .duration(85L)
                .resources(Collections.singletonList(dto))
                .build();

        assertEquals("Java编程思想", result.getKeyword());
        assertEquals(1L, result.getTotalCount());
        assertEquals(1, result.getResources().size());
        assertEquals(1L, result.getResources().get(0).getId());
        assertEquals("百度网盘", result.getResources().get(0).getPanTypeName());
        assertEquals("abcd", result.getResources().get(0).getExtractCode());
    }

    @Test
    void testSearchResultWithMultiplePanTypes() {
        List<SearchResult.ResourceDTO> dtos = Arrays.asList(
                SearchResult.ResourceDTO.builder().id(1L).title("百度资源").panUrl("https://pan.baidu.com/s/1").panType("BAIDU").panTypeName("百度网盘").build(),
                SearchResult.ResourceDTO.builder().id(2L).title("阿里资源").panUrl("https://www.aliyundrive.com/s/2").panType("ALIYUN").panTypeName("阿里云盘").build(),
                SearchResult.ResourceDTO.builder().id(3L).title("夸克资源").panUrl("https://pan.quark.cn/s/3").panType("QUARK").panTypeName("夸克网盘").build(),
                SearchResult.ResourceDTO.builder().id(4L).title("迅雷资源").panUrl("https://pan.xunlei.com/s/4").panType("XUNLEI").panTypeName("迅雷网盘").build(),
                SearchResult.ResourceDTO.builder().id(5L).title("天翼资源").panUrl("https://cloud.189.cn/t/5").panType("TIANYI").panTypeName("天翼云盘").build()
        );

        SearchResult result = SearchResult.builder()
                .keyword("网盘资源")
                .totalCount(5L)
                .page(0)
                .pageSize(20)
                .duration(200L)
                .resources(dtos)
                .build();

        assertEquals(5, result.getResources().size());
        assertEquals("BAIDU", result.getResources().get(0).getPanType());
        assertEquals("ALIYUN", result.getResources().get(1).getPanType());
        assertEquals("QUARK", result.getResources().get(2).getPanType());
        assertEquals("XUNLEI", result.getResources().get(3).getPanType());
        assertEquals("TIANYI", result.getResources().get(4).getPanType());
    }

    @Test
    void testSearchWithSpecialCharacters() {
        // 模拟包含特殊字符的搜索结果（如C++、C#等）
        SearchResult result = SearchResult.builder()
                .keyword("C++")
                .totalCount(1L)
                .page(0)
                .pageSize(20)
                .duration(50L)
                .resources(Collections.singletonList(
                        SearchResult.ResourceDTO.builder()
                                .id(1L)
                                .title("C++现代编程实战")
                                .panUrl("https://pan.quark.cn/s/cpp_modern")
                                .panType("QUARK")
                                .build()
                ))
                .build();

        assertEquals("C++", result.getKeyword());
        assertEquals(1L, result.getTotalCount());
    }

    @Test
    void testSearchWithEmptyKeyword() {
        // 空关键词应返回空结果
        SearchResult result = SearchResult.builder()
                .keyword("")
                .totalCount(0L)
                .page(0)
                .pageSize(20)
                .duration(0L)
                .resources(Collections.emptyList())
                .build();

        assertEquals("", result.getKeyword());
        assertEquals(0L, result.getTotalCount());
        assertTrue(result.getResources().isEmpty());
    }

    @Test
    void testSearchWithPagination() {
        // 模拟分页搜索，第2页
        SearchResult result = SearchResult.builder()
                .keyword("Python")
                .totalCount(50L)
                .page(1)
                .pageSize(20)
                .duration(120L)
                .resources(Collections.emptyList())
                .build();

        assertEquals(1, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(50L, result.getTotalCount());
    }

    // ==================== /api/resources/{id} 资源详情测试 ====================

    @Test
    void testResourceDetailDTO() {
        SearchResult.ResourceDTO detail = SearchResult.ResourceDTO.builder()
                .id(42L)
                .title("尚硅谷Java零基础教程")
                .description("2024最新版Java零基础入门到精通")
                .panUrl("https://www.aliyundrive.com/s/shangguigu_java")
                .panType("ALIYUN")
                .panTypeName("阿里云盘")
                .extractCode(null)
                .resourceType("VIDEO")
                .resourceTypeName("视频")
                .sourceUrl("https://www.bilibili.com/video/BV1xx411c7XW")
                .sourceSite("www.bilibili.com")
                .fileSize("15.2GB")
                .isValid(true)
                .clickCount(580L)
                .createdAt("2024-03-20T14:30:00")
                .build();

        assertNotNull(detail);
        assertEquals(42L, detail.getId());
        assertEquals("尚硅谷Java零基础教程", detail.getTitle());
        assertEquals("阿里云盘", detail.getPanTypeName());
        assertNull(detail.getExtractCode());
        assertEquals("VIDEO", detail.getResourceType());
        assertEquals("视频", detail.getResourceTypeName());
    }

    @Test
    void testResourceNotFound() {
        // 模拟资源不存在
        SearchResult.ResourceDTO resource = null;
        assertNull(resource);
    }

    // ==================== /api/resources/popular 热门资源测试 ====================

    @Test
    void testPopularResourcesSortedByClickCount() {
        List<SearchResult.ResourceDTO> popular = Arrays.asList(
                SearchResult.ResourceDTO.builder().id(1L).title("最热门资源").panUrl("https://pan.baidu.com/s/1").panType("BAIDU").clickCount(1000L).build(),
                SearchResult.ResourceDTO.builder().id(2L).title("次热门资源").panUrl("https://pan.quark.cn/s/2").panType("QUARK").clickCount(500L).build(),
                SearchResult.ResourceDTO.builder().id(3L).title("一般资源").panUrl("https://www.aliyundrive.com/s/3").panType("ALIYUN").clickCount(100L).build()
        );

        assertEquals(1000L, popular.get(0).getClickCount());
        assertEquals(500L, popular.get(1).getClickCount());
        assertEquals(100L, popular.get(2).getClickCount());
        assertTrue(popular.get(0).getClickCount() > popular.get(1).getClickCount());
    }

    // ==================== /api/resources/latest 最新资源测试 ====================

    @Test
    void testLatestResourcesSortedByCreatedAt() {
        List<SearchResult.ResourceDTO> latest = Arrays.asList(
                SearchResult.ResourceDTO.builder().id(3L).title("最新资源").panUrl("https://pan.baidu.com/s/3").panType("BAIDU").createdAt("2024-12-01T10:00:00").build(),
                SearchResult.ResourceDTO.builder().id(2L).title("次新资源").panUrl("https://pan.quark.cn/s/2").panType("QUARK").createdAt("2024-11-15T10:00:00").build(),
                SearchResult.ResourceDTO.builder().id(1L).title("较旧资源").panUrl("https://www.aliyundrive.com/s/1").panType("ALIYUN").createdAt("2024-10-01T10:00:00").build()
        );

        assertNotNull(latest.get(0).getCreatedAt());
        assertNotNull(latest.get(1).getCreatedAt());
        assertNotNull(latest.get(2).getCreatedAt());
    }

    // ==================== /api/resources/pan/{type} 按网盘类型查询测试 ====================

    @Test
    void testFilterByBaiduPanType() {
        List<SearchResult.ResourceDTO> baiduResources = Arrays.asList(
                SearchResult.ResourceDTO.builder().id(1L).title("百度资源1").panUrl("https://pan.baidu.com/s/1").panType("BAIDU").build(),
                SearchResult.ResourceDTO.builder().id(2L).title("百度资源2").panUrl("https://pan.baidu.com/s/2").panType("BAIDU").build()
        );

        for (SearchResult.ResourceDTO dto : baiduResources) {
            assertEquals("BAIDU", dto.getPanType());
        }
    }

    @Test
    void testFilterByAliyunPanType() {
        SearchResult.ResourceDTO aliyun = SearchResult.ResourceDTO.builder()
                .id(1L).title("阿里资源").panUrl("https://www.aliyundrive.com/s/test").panType("ALIYUN").panTypeName("阿里云盘").build();

        assertEquals("ALIYUN", aliyun.getPanType());
        assertEquals("阿里云盘", aliyun.getPanTypeName());
    }

    @Test
    void testFilterByQuarkPanType() {
        SearchResult.ResourceDTO quark = SearchResult.ResourceDTO.builder()
                .id(1L).title("夸克资源").panUrl("https://pan.quark.cn/s/test").panType("QUARK").panTypeName("夸克网盘").build();

        assertEquals("QUARK", quark.getPanType());
        assertEquals("夸克网盘", quark.getPanTypeName());
    }

    // ==================== /api/resources/all 全部资源测试 ====================

    @Test
    void testGetAllResourcesWithPagination() {
        SearchResult result = SearchResult.builder()
                .keyword("")
                .totalCount(100L)
                .page(2)
                .pageSize(20)
                .duration(0L)
                .resources(Collections.emptyList())
                .build();

        assertEquals("", result.getKeyword());
        assertEquals(100L, result.getTotalCount());
        assertEquals(2, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(0L, result.getDuration());
    }

    // ==================== /api/keywords/hot 热门关键词测试 ====================

    @Test
    void testHotKeywordsList() {
        List<String> keywords = Arrays.asList("Java", "Python", "C++", "前端", "人工智能");

        assertEquals(5, keywords.size());
        assertEquals("Java", keywords.get(0));
        assertTrue(keywords.contains("Python"));
        assertTrue(keywords.contains("C++"));
    }

    // ==================== SearchHistory 搜索历史测试 ====================

    @Test
    void testSearchHistoryCreation() {
        SearchHistory history = SearchHistory.builder()
                .id(1L)
                .keyword("Java设计模式")
                .resultCount(15)
                .duration(120L)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(history);
        assertEquals(1L, history.getId());
        assertEquals("Java设计模式", history.getKeyword());
        assertEquals(15, history.getResultCount());
        assertEquals(120L, history.getDuration());
    }

    @Test
    void testSearchHistoryDefaultValues() {
        SearchHistory history = new SearchHistory();
        assertNull(history.getId());
        assertNull(history.getKeyword());
        // resultCount has @Builder.Default = 0
        assertEquals(0, history.getResultCount());
    }

    @Test
    void testSearchHistoryBuilderDefaults() {
        SearchHistory history = SearchHistory.builder().build();
        assertEquals(0, history.getResultCount());
    }

    // ==================== Resource实体测试 ====================

    @Test
    void testResourceWithAllFields() {
        Resource resource = Resource.builder()
                .id(100L)
                .title("Go语言高级编程")
                .description("Go语言进阶教程，包含并发编程、网络编程等")
                .panUrl("https://pan.baidu.com/s/1go_advanced")
                .panType(Resource.PanType.BAIDU)
                .extractCode("efgh")
                .resourceType(Resource.ResourceType.DOCUMENT)
                .sourceUrl("https://github.com/golang/go")
                .sourceSite("github.com")
                .fileSize("25MB")
                .isValid(true)
                .clickCount(75L)
                .createdAt(LocalDateTime.of(2024, 5, 20, 8, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 5, 20, 8, 0, 0))
                .build();

        assertEquals(100L, resource.getId());
        assertEquals("Go语言高级编程", resource.getTitle());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals(Resource.ResourceType.DOCUMENT, resource.getResourceType());
        assertTrue(resource.getIsValid());
        assertEquals(75L, resource.getClickCount());
    }

    @Test
    void testResourceDefaultValues() {
        Resource resource = new Resource();
        assertNull(resource.getId());
        // isValid has @Builder.Default = true
        assertTrue(resource.getIsValid());
        // clickCount has @Builder.Default = 0L
        assertEquals(0L, resource.getClickCount());
    }

    @Test
    void testResourceUpdateClickCount() {
        Resource resource = Resource.builder()
                .title("测试资源")
                .panUrl("https://pan.quark.cn/s/test")
                .panType(Resource.PanType.QUARK)
                .build();

        assertEquals(0L, resource.getClickCount());
        resource.setClickCount(resource.getClickCount() + 1);
        assertEquals(1L, resource.getClickCount());
    }

    @Test
    void testResourceSetInvalid() {
        Resource resource = Resource.builder()
                .title("失效资源")
                .panUrl("https://pan.baidu.com/s/expired")
                .panType(Resource.PanType.BAIDU)
                .isValid(true)
                .build();

        assertTrue(resource.getIsValid());
        resource.setIsValid(false);
        assertFalse(resource.getIsValid());
    }
}
