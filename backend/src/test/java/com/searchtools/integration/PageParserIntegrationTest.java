package com.searchtools.integration;

import com.searchtools.model.Resource;
import com.searchtools.parser.PageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 页面解析集成测试 - 纯JUnit测试，不依赖Spring上下文
 */
class PageParserIntegrationTest {

    private PageParser pageParser;

    @BeforeEach
    void setUp() {
        pageParser = new PageParser();
    }

    @Test
    void testParseBaiduPanLink() {
        String html = "<html><head><title>测试资源分享</title></head><body>"
                + "<p>这是一个测试资源</p>"
                + "<p>百度网盘链接: https://pan.baidu.com/s/1abcdefg</p>"
                + "<p>提取码: abcd</p>"
                + "</body></html>";

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        Resource resource = resources.get(0);
        assertEquals("https://pan.baidu.com/s/1abcdefg", resource.getPanUrl());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals("abcd", resource.getExtractCode());
        assertNotNull(resource.getTitle());
    }

    @Test
    void testParseAliyunDriveLink() {
        String html = "<html><head><title>阿里云盘分享</title></head><body>"
                + "<p>阿里云盘链接: https://www.aliyundrive.com/s/abc123</p>"
                + "</body></html>";

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        Resource resource = resources.get(0);
        assertEquals("https://www.aliyundrive.com/s/abc123", resource.getPanUrl());
        assertEquals(Resource.PanType.ALIYUN, resource.getPanType());
    }

    @Test
    void testParseQuarkPanLink() {
        String html = "<html><head><title>夸克网盘分享</title></head><body>"
                + "<p>夸克网盘链接: https://pan.quark.cn/s/xyz789</p>"
                + "</body></html>";

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        Resource resource = resources.get(0);
        assertEquals("https://pan.quark.cn/s/xyz789", resource.getPanUrl());
        assertEquals(Resource.PanType.QUARK, resource.getPanType());
    }

    @Test
    void testParseMultiplePanLinks() {
        String html = "<html><head><title>多网盘资源</title></head><body>"
                + "<h1>电影资源下载</h1>"
                + "<p>百度网盘: https://pan.baidu.com/s/1test1 提取码: 1111</p>"
                + "<p>阿里云盘: https://www.aliyundrive.com/s/test2</p>"
                + "<p>夸克网盘: https://pan.quark.cn/s/test3</p>"
                + "</body></html>";

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertEquals(3, resources.size());

        Resource baiduResource = resources.stream()
                .filter(r -> r.getPanType() == Resource.PanType.BAIDU)
                .findFirst().orElse(null);
        assertNotNull(baiduResource);
        assertEquals("https://pan.baidu.com/s/1test1", baiduResource.getPanUrl());
        assertEquals("1111", baiduResource.getExtractCode());

        Resource aliyunResource = resources.stream()
                .filter(r -> r.getPanType() == Resource.PanType.ALIYUN)
                .findFirst().orElse(null);
        assertNotNull(aliyunResource);
        assertEquals("https://www.aliyundrive.com/s/test2", aliyunResource.getPanUrl());

        Resource quarkResource = resources.stream()
                .filter(r -> r.getPanType() == Resource.PanType.QUARK)
                .findFirst().orElse(null);
        assertNotNull(quarkResource);
        assertEquals("https://pan.quark.cn/s/test3", quarkResource.getPanUrl());
    }

    @Test
    void testParseResourceTypes() {
        // 测试视频类型识别
        String videoHtml = "<html><head><title>电影资源下载</title></head><body>"
                + "<p>高清电影资源</p>"
                + "<p>百度网盘: https://pan.baidu.com/s/1movie1</p>"
                + "</body></html>";

        Document videoDoc = Jsoup.parse(videoHtml, "https://example.com");
        List<Resource> videoResources = pageParser.parsePage("https://example.com/test", videoDoc);
        assertFalse(videoResources.isEmpty());
        assertEquals(Resource.ResourceType.VIDEO, videoResources.get(0).getResourceType());

        // 测试软件类型识别
        String softwareHtml = "<html><head><title>软件下载</title></head><body>"
                + "<p>最新软件下载</p>"
                + "<p>百度网盘: https://pan.baidu.com/s/1software1</p>"
                + "</body></html>";

        Document softwareDoc = Jsoup.parse(softwareHtml, "https://example.com");
        List<Resource> softwareResources = pageParser.parsePage("https://example.com/test", softwareDoc);
        assertFalse(softwareResources.isEmpty());
        assertEquals(Resource.ResourceType.SOFTWARE, softwareResources.get(0).getResourceType());

        // 测试文档类型识别
        String documentHtml = "<html><head><title>PDF文档下载</title></head><body>"
                + "<p>电子书下载</p>"
                + "<p>阿里云盘: https://www.aliyundrive.com/s/doc1</p>"
                + "</body></html>";

        Document docDoc = Jsoup.parse(documentHtml, "https://example.com");
        List<Resource> docResources = pageParser.parsePage("https://example.com/test", docDoc);
        assertFalse(docResources.isEmpty());
        assertEquals(Resource.ResourceType.DOCUMENT, docResources.get(0).getResourceType());
    }

    @Test
    void testParseEmptyPage() {
        String html = "<html><head><title>空页面</title></head><body>"
                + "<p>这是一个普通页面，没有网盘链接</p>"
                + "</body></html>";

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertTrue(resources.isEmpty());
    }

    @Test
    void testParseExtractCodePatterns() {
        String html = "<html><head><title>提取码测试</title></head><body>"
                + "<p>百度网盘1: https://pan.baidu.com/s/1test1 提取码: abcd</p>"
                + "<p>百度网盘2: https://pan.baidu.com/s/1test2 提取码: efgh</p>"
                + "<p>百度网盘3: https://pan.baidu.com/s/1test3</p>"
                + "</body></html>";

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertEquals(3, resources.size());

        // 验证有提取码的资源确实提取到了提取码
        long resourcesWithCode = resources.stream()
                .filter(r -> r.getExtractCode() != null && !r.getExtractCode().isEmpty())
                .count();
        assertTrue(resourcesWithCode >= 1, "至少应有一个资源有提取码");
    }

    @Test
    void testSourceUrlPreserved() {
        String html = "<html><head><title>测试</title></head><body>"
                + "<p>百度网盘: https://pan.baidu.com/s/1test1</p>"
                + "</body></html>";

        String sourceUrl = "https://forum.example.com/thread/123";
        Document document = Jsoup.parse(html, sourceUrl);
        List<Resource> resources = pageParser.parsePage(sourceUrl, document);

        assertFalse(resources.isEmpty());
        assertEquals(sourceUrl, resources.get(0).getSourceUrl());
    }
}
