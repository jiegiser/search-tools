package com.searchtools.parser;

import com.searchtools.model.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageParserTest {

    private PageParser pageParser;

    @BeforeEach
    void setUp() {
        pageParser = new PageParser();
    }

    @Test
    void testParseBaiduPanLink() {
        String html = """
                <html>
                <head><title>测试资源分享</title></head>
                <body>
                    <p>这是一个测试资源</p>
                    <p>百度网盘链接: https://pan.baidu.com/s/1abcdefg</p>
                    <p>提取码: abcd</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        Resource resource = resources.get(0);
        assertEquals("https://pan.baidu.com/s/1abcdefg", resource.getPanUrl());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals("abcd", resource.getExtractCode());
    }

    @Test
    void testParseAliyunDriveLink() {
        String html = """
                <html>
                <head><title>阿里云盘分享</title></head>
                <body>
                    <p>阿里云盘链接: https://www.aliyundrive.com/s/abc123</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        Resource resource = resources.get(0);
        assertEquals("https://www.aliyundrive.com/s/abc123", resource.getPanUrl());
        assertEquals(Resource.PanType.ALIYUN, resource.getPanType());
    }

    @Test
    void testParseQuarkPanLink() {
        String html = """
                <html>
                <head><title>夸克网盘分享</title></head>
                <body>
                    <p>夸克网盘链接: https://pan.quark.cn/s/xyz789</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        Resource resource = resources.get(0);
        assertEquals("https://pan.quark.cn/s/xyz789", resource.getPanUrl());
        assertEquals(Resource.PanType.QUARK, resource.getPanType());
    }

    @Test
    void testParseMultipleLinks() {
        String html = """
                <html>
                <head><title>多网盘资源</title></head>
                <body>
                    <p>百度网盘: https://pan.baidu.com/s/1test1</p>
                    <p>阿里云盘: https://www.aliyundrive.com/s/test2</p>
                    <p>夸克网盘: https://pan.quark.cn/s/test3</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertEquals(3, resources.size());
    }

    @Test
    void testParseNoLinks() {
        String html = """
                <html>
                <head><title>无链接页面</title></head>
                <body>
                    <p>这是一个普通页面，没有网盘链接</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertTrue(resources.isEmpty());
    }

    @Test
    void testInferResourceType() {
        String html = """
                <html>
                <head><title>电影资源下载</title></head>
                <body>
                    <p>高清电影资源</p>
                    <p>百度网盘: https://pan.baidu.com/s/1movie1</p>
                </body>
                </html>
                """;

        Document document = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com/test", document);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.ResourceType.VIDEO, resources.get(0).getResourceType());
    }
}
