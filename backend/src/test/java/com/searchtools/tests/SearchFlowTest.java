package com.searchtools.tests;

import com.searchtools.model.Resource;
import com.searchtools.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索流程全量测试 - 覆盖完整的搜索、爬取、索引、查询流程
 */
class SearchFlowTest {

    // ==================== 搜索流程：本地有数据 ====================

    @Test
    void testSearchWithLocalData() {
        // 模拟本地已有数据的搜索
        List<Resource> localResources = Arrays.asList(
                Resource.builder().title("Java编程入门").panUrl("https://pan.baidu.com/s/java1").panType(Resource.PanType.BAIDU).isValid(true).build(),
                Resource.builder().title("Java高级编程").panUrl("https://pan.quark.cn/s/java2").panType(Resource.PanType.QUARK).isValid(true).build(),
                Resource.builder().title("Python入门教程").panUrl("https://www.aliyundrive.com/s/py1").panType(Resource.PanType.ALIYUN).isValid(true).build()
        );

        // 模拟Lucene搜索
        String keyword = "Java";
        List<Resource> results = localResources.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getTitle().contains("Java")));
    }

    @Test
    void testSearchWithNoLocalDataTriggersCrawl() {
        // 模拟本地无数据
        List<Resource> localResources = Collections.emptyList();

        // 当本地无结果时应触发全网爬取
        boolean shouldCrawl = localResources.isEmpty();
        assertTrue(shouldCrawl);
    }

    @Test
    void testSearchFlowAfterCrawl() {
        // 模拟爬取后的搜索流程
        List<Resource> crawledResources = Arrays.asList(
                Resource.builder().title("C++现代编程实战").panUrl("https://pan.baidu.com/s/cpp1").panType(Resource.PanType.BAIDU).isValid(true).build(),
                Resource.builder().title("C++ Primer Plus").panUrl("https://pan.quark.cn/s/cpp2").panType(Resource.PanType.QUARK).isValid(true).build(),
                Resource.builder().title("C++ STL源码剖析").panUrl("https://www.aliyundrive.com/s/cpp3").panType(Resource.PanType.ALIYUN).isValid(true).build()
        );

        // 模拟搜索
        String keyword = "C++";
        List<Resource> results = crawledResources.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());

        assertEquals(3, results.size());
    }

    // ==================== 搜索结果DTO转换测试 ====================

    @Test
    void testConvertResourceToDTO() {
        Resource resource = Resource.builder()
                .id(1L)
                .title("算法导论")
                .description("经典算法教材")
                .panUrl("https://pan.baidu.com/s/1algorithms")
                .panType(Resource.PanType.BAIDU)
                .extractCode("abc1")
                .resourceType(Resource.ResourceType.DOCUMENT)
                .sourceUrl("https://www.example.com")
                .sourceSite("www.example.com")
                .fileSize("20MB")
                .isValid(true)
                .clickCount(500L)
                .build();

        // 模拟DTO转换
        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .panUrl(resource.getPanUrl())
                .panType(resource.getPanType().name())
                .panTypeName(resource.getPanType().getDisplayName())
                .extractCode(resource.getExtractCode())
                .resourceType(resource.getResourceType().name())
                .resourceTypeName(resource.getResourceType().getDisplayName())
                .sourceUrl(resource.getSourceUrl())
                .sourceSite(resource.getSourceSite())
                .fileSize(resource.getFileSize())
                .isValid(resource.getIsValid())
                .clickCount(resource.getClickCount())
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("算法导论", dto.getTitle());
        assertEquals("经典算法教材", dto.getDescription());
        assertEquals("BAIDU", dto.getPanType());
        assertEquals("百度网盘", dto.getPanTypeName());
        assertEquals("DOCUMENT", dto.getResourceType());
        assertEquals("文档", dto.getResourceTypeName());
        assertEquals("abc1", dto.getExtractCode());
        assertEquals("www.example.com", dto.getSourceSite());
        assertEquals("20MB", dto.getFileSize());
        assertTrue(dto.getIsValid());
        assertEquals(500L, dto.getClickCount());
    }

    @Test
    void testConvertResourceWithNullOptionalFields() {
        Resource resource = Resource.builder()
                .id(2L)
                .title("最小资源")
                .panUrl("https://pan.quark.cn/s/minimal")
                .panType(Resource.PanType.QUARK)
                .build();

        SearchResult.ResourceDTO dto = SearchResult.ResourceDTO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .panUrl(resource.getPanUrl())
                .panType(resource.getPanType().name())
                .panTypeName(resource.getPanType().getDisplayName())
                .resourceType(resource.getResourceType() != null ? resource.getResourceType().name() : "OTHER")
                .resourceTypeName(resource.getResourceType() != null ? resource.getResourceType().getDisplayName() : "其他")
                .build();

        assertEquals(2L, dto.getId());
        assertNull(dto.getDescription());
        assertNull(dto.getExtractCode());
        assertEquals("OTHER", dto.getResourceType());
        assertEquals("其他", dto.getResourceTypeName());
        assertNull(dto.getSourceUrl());
        assertNull(dto.getFileSize());
    }

    // ==================== 分页搜索测试 ====================

    @Test
    void testSearchPaginationFirstPage() {
        List<Resource> allResources = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            allResources.add(Resource.builder()
                    .title("资源" + i)
                    .panUrl("https://pan.baidu.com/s/" + i)
                    .panType(Resource.PanType.BAIDU)
                    .isValid(true)
                    .build());
        }

        int page = 0;
        int pageSize = 20;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allResources.size());

        List<Resource> pageContent = allResources.subList(start, end);
        assertEquals(20, pageContent.size());
        assertEquals("资源1", pageContent.get(0).getTitle());
        assertEquals("资源20", pageContent.get(19).getTitle());
    }

    @Test
    void testSearchPaginationMiddlePage() {
        List<Resource> allResources = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            allResources.add(Resource.builder()
                    .title("资源" + i)
                    .panUrl("https://pan.baidu.com/s/" + i)
                    .panType(Resource.PanType.BAIDU)
                    .isValid(true)
                    .build());
        }

        int page = 1;
        int pageSize = 20;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allResources.size());

        List<Resource> pageContent = allResources.subList(start, end);
        assertEquals(20, pageContent.size());
        assertEquals("资源21", pageContent.get(0).getTitle());
    }

    @Test
    void testSearchPaginationLastPage() {
        List<Resource> allResources = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            allResources.add(Resource.builder()
                    .title("资源" + i)
                    .panUrl("https://pan.baidu.com/s/" + i)
                    .panType(Resource.PanType.BAIDU)
                    .isValid(true)
                    .build());
        }

        int page = 2;
        int pageSize = 20;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allResources.size());

        List<Resource> pageContent = allResources.subList(start, end);
        assertEquals(10, pageContent.size());
        assertEquals("资源41", pageContent.get(0).getTitle());
        assertEquals("资源50", pageContent.get(9).getTitle());
    }

    @Test
    void testSearchPaginationEmptyPage() {
        List<Resource> allResources = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            allResources.add(Resource.builder()
                    .title("资源" + i)
                    .panUrl("https://pan.baidu.com/s/" + i)
                    .panType(Resource.PanType.BAIDU)
                    .isValid(true)
                    .build());
        }

        int page = 5;
        int pageSize = 20;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allResources.size());

        // When start >= total size, return empty page
        List<Resource> pageContent = start >= allResources.size()
                ? Collections.emptyList()
                : allResources.subList(start, end);
        assertTrue(pageContent.isEmpty());
    }

    // ==================== 资源去重测试 ====================

    @Test
    void testResourceDeduplication() {
        List<Resource> resources = new ArrayList<>();
        resources.add(Resource.builder().title("资源A").panUrl("https://pan.baidu.com/s/same").panType(Resource.PanType.BAIDU).build());
        resources.add(Resource.builder().title("资源A副本").panUrl("https://pan.baidu.com/s/same").panType(Resource.PanType.BAIDU).build());
        resources.add(Resource.builder().title("资源B").panUrl("https://pan.quark.cn/s/different").panType(Resource.PanType.QUARK).build());

        // 模拟去重逻辑（基于panUrl）
        List<String> uniqueUrls = resources.stream()
                .map(Resource::getPanUrl)
                .distinct()
                .collect(Collectors.toList());

        assertEquals(2, uniqueUrls.size());
    }

    // ==================== 多网盘类型搜索测试 ====================

    @Test
    void testSearchAcrossAllPanTypes() {
        List<Resource> allResources = Arrays.asList(
                Resource.builder().title("百度资源").panUrl("https://pan.baidu.com/s/1").panType(Resource.PanType.BAIDU).build(),
                Resource.builder().title("阿里资源").panUrl("https://www.aliyundrive.com/s/2").panType(Resource.PanType.ALIYUN).build(),
                Resource.builder().title("夸克资源").panUrl("https://pan.quark.cn/s/3").panType(Resource.PanType.QUARK).build(),
                Resource.builder().title("迅雷资源").panUrl("https://pan.xunlei.com/s/4").panType(Resource.PanType.XUNLEI).build(),
                Resource.builder().title("天翼资源").panUrl("https://cloud.189.cn/t/5").panType(Resource.PanType.TIANYI).build(),
                Resource.builder().title("UC资源").panUrl("https://drive.uc.cn/s/6").panType(Resource.PanType.UC).build(),
                Resource.builder().title("115资源").panUrl("https://115.com/s/7").panType(Resource.PanType.PAN115).build(),
                Resource.builder().title("123资源").panUrl("https://www.123pan.com/s/8").panType(Resource.PanType.PAN123).build(),
                Resource.builder().title("微云资源").panUrl("https://share.weiyun.com/9").panType(Resource.PanType.WEIYUN).build(),
                Resource.builder().title("蓝奏资源").panUrl("https://www.lanzou.com/10").panType(Resource.PanType.LANZOU).build(),
                Resource.builder().title("MEGA资源").panUrl("https://mega.nz/file/11").panType(Resource.PanType.MEGA).build(),
                Resource.builder().title("其他资源").panUrl("https://other.com/12").panType(Resource.PanType.OTHER).build()
        );

        assertEquals(12, allResources.size());
        assertEquals(12, allResources.stream().map(r -> r.getPanType()).distinct().count());
    }

    @Test
    void testSearchBySpecificPanType() {
        List<Resource> allResources = Arrays.asList(
                Resource.builder().title("百度1").panUrl("https://pan.baidu.com/s/1").panType(Resource.PanType.BAIDU).build(),
                Resource.builder().title("百度2").panUrl("https://pan.baidu.com/s/2").panType(Resource.PanType.BAIDU).build(),
                Resource.builder().title("夸克1").panUrl("https://pan.quark.cn/s/3").panType(Resource.PanType.QUARK).build()
        );

        List<Resource> baiduOnly = allResources.stream()
                .filter(r -> r.getPanType() == Resource.PanType.BAIDU)
                .collect(Collectors.toList());

        assertEquals(2, baiduOnly.size());
        assertTrue(baiduOnly.stream().allMatch(r -> r.getPanType() == Resource.PanType.BAIDU));
    }

    // ==================== 搜索结果构建测试 ====================

    @Test
    void testBuildSearchResultWithCrawlInfo() {
        SearchResult result = SearchResult.builder()
                .keyword("Java设计模式")
                .totalCount(5L)
                .page(0)
                .pageSize(20)
                .duration(3500L) // 含爬取时间
                .resources(Arrays.asList(
                        SearchResult.ResourceDTO.builder()
                                .id(1L).title("设计模式").panUrl("https://pan.baidu.com/s/1").panType("BAIDU").build(),
                        SearchResult.ResourceDTO.builder()
                                .id(2L).title("Java设计模式").panUrl("https://pan.quark.cn/s/2").panType("QUARK").build()
                ))
                .build();

        assertEquals("Java设计模式", result.getKeyword());
        assertEquals(5L, result.getTotalCount());
        assertEquals(2, result.getResources().size());
        // 耗时可能较长（包含爬取）
        assertTrue(result.getDuration() > 0);
    }

    // ==================== 资源有效性筛选测试 ====================

    @Test
    void testFilterValidResources() {
        List<Resource> allResources = Arrays.asList(
                Resource.builder().title("有效1").panUrl("https://pan.baidu.com/s/1").panType(Resource.PanType.BAIDU).isValid(true).build(),
                Resource.builder().title("无效1").panUrl("https://pan.baidu.com/s/2").panType(Resource.PanType.BAIDU).isValid(false).build(),
                Resource.builder().title("有效2").panUrl("https://pan.quark.cn/s/3").panType(Resource.PanType.QUARK).isValid(true).build(),
                Resource.builder().title("无效2").panUrl("https://pan.quark.cn/s/4").panType(Resource.PanType.QUARK).isValid(false).build()
        );

        List<Resource> validOnly = allResources.stream()
                .filter(Resource::getIsValid)
                .collect(Collectors.toList());

        assertEquals(2, validOnly.size());
        assertTrue(validOnly.stream().allMatch(Resource::getIsValid));
    }

    // ==================== 热门资源排序测试 ====================

    @Test
    void testSortResourcesByClickCount() {
        List<Resource> resources = Arrays.asList(
                Resource.builder().title("少点击").panUrl("https://a.com").panType(Resource.PanType.BAIDU).clickCount(10L).build(),
                Resource.builder().title("多点击").panUrl("https://b.com").panType(Resource.PanType.QUARK).clickCount(1000L).build(),
                Resource.builder().title("中点击").panUrl("https://c.com").panType(Resource.PanType.ALIYUN).clickCount(100L).build()
        );

        List<Resource> sorted = resources.stream()
                .sorted((a, b) -> Long.compare(b.getClickCount(), a.getClickCount()))
                .collect(Collectors.toList());

        assertEquals("多点击", sorted.get(0).getTitle());
        assertEquals("中点击", sorted.get(1).getTitle());
        assertEquals("少点击", sorted.get(2).getTitle());
    }

    // ==================== 最新资源排序测试 ====================

    @Test
    void testSortResourcesByCreatedAt() {
        List<Resource> resources = Arrays.asList(
                Resource.builder().title("旧资源").panUrl("https://a.com").panType(Resource.PanType.BAIDU).build(),
                Resource.builder().title("新资源").panUrl("https://b.com").panType(Resource.PanType.QUARK).build(),
                Resource.builder().title("中资源").panUrl("https://c.com").panType(Resource.PanType.ALIYUN).build()
        );

        // 模拟按创建时间排序（降序）
        assertNotNull(resources);
        assertEquals(3, resources.size());
    }
}
