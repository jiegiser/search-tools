package com.searchtools.tests;

import com.searchtools.model.Resource;
import com.searchtools.parser.PageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageParser全面测试 - 测试各种网盘链接解析
 */
class PageParserComprehensiveTest {

    private PageParser pageParser;

    @BeforeEach
    void setUp() {
        pageParser = new PageParser();
    }

    @Test
    void testParseBaiduPanLink() {
        String html = "<html><head><title>Java教程分享</title></head><body>" +
                "<p>百度网盘链接: https://pan.baidu.com/s/1abc123 提取码: abcd</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        Resource r = resources.get(0);
        assertEquals("https://pan.baidu.com/s/1abc123", r.getPanUrl());
        assertEquals(Resource.PanType.BAIDU, r.getPanType());
    }

    @Test
    void testParseAliyunDriveLink() {
        String html = "<html><head><title>阿里云盘分享</title></head><body>" +
                "<p>阿里云盘: https://www.alipan.com/s/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.ALIYUN, resources.get(0).getPanType());
    }

    @Test
    void testParseAliyunDriveOldDomain() {
        String html = "<html><head><title>旧域名</title></head><body>" +
                "<p>https://www.aliyundrive.com/s/oldlink123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.ALIYUN, resources.get(0).getPanType());
    }

    @Test
    void testParseQuarkPanLink() {
        String html = "<html><head><title>夸克分享</title></head><body>" +
                "<p>夸克网盘: https://pan.quark.cn/s/abc123def</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.QUARK, resources.get(0).getPanType());
    }

    @Test
    void testParseXunleiPanLink() {
        String html = "<html><head><title>迅雷分享</title></head><body>" +
                "<p>https://pan.xunlei.com/s/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.XUNLEI, resources.get(0).getPanType());
    }

    @Test
    void testParseTianyiPanLink() {
        String html = "<html><head><title>天翼分享</title></head><body>" +
                "<p>https://cloud.189.cn/t/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.TIANYI, resources.get(0).getPanType());
    }

    @Test
    void testParseMegaLink() {
        String html = "<html><head><title>MEGA分享</title></head><body>" +
                "<p>https://mega.nz/file/abc123_def</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.MEGA, resources.get(0).getPanType());
    }

    @Test
    void testParseLanzouLink() {
        String html = "<html><head><title>蓝奏云分享</title></head><body>" +
                "<p>https://www.lanzouw.com/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.LANZOU, resources.get(0).getPanType());
    }

    @Test
    void testParseMultipleLinks() {
        String html = "<html><head><title>多链接分享</title></head><body>" +
                "<p>百度: https://pan.baidu.com/s/1multi1</p>" +
                "<p>阿里: https://www.alipan.com/s/multi2</p>" +
                "<p>夸克: https://pan.quark.cn/s/multi3</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertTrue(resources.size() >= 3);
    }

    @Test
    void testExtractCode() {
        String html = "<html><head><title>带提取码</title></head><body>" +
                "<p>链接: https://pan.baidu.com/s/1withcode</p>" +
                "<p>提取码: abcd</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals("abcd", resources.get(0).getExtractCode());
    }

    @Test
    void testExtractCodeWithChineseColon() {
        String html = "<html><head><title>中文冒号</title></head><body>" +
                "<p>https://pan.baidu.com/s/1cnccode</p>" +
                "<p>密码：xyz12345</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals("xyz12345", resources.get(0).getExtractCode());
    }

    @Test
    void testNoExtractCode() {
        String html = "<html><head><title>无提取码</title></head><body>" +
                "<p>https://pan.quark.cn/s/nocode</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertNull(resources.get(0).getExtractCode());
    }

    @Test
    void testParseEmptyPage() {
        String html = "<html><head><title>空页面</title></head><body><p>没有任何链接</p></body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertTrue(resources.isEmpty());
    }

    @Test
    void testParseNullDocument() {
        List<Resource> resources = pageParser.parsePage("https://example.com", null);
        assertTrue(resources.isEmpty());
    }

    @Test
    void testSourceSiteExtraction() {
        String html = "<html><head><title>来源测试</title></head><body>" +
                "<p>https://pan.baidu.com/s/1source</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://www.example.com/page/1");
        List<Resource> resources = pageParser.parsePage("https://www.example.com/page/1", doc);

        assertFalse(resources.isEmpty());
        assertEquals("www.example.com", resources.get(0).getSourceSite());
    }

    @Test
    void testResourceTypeInferenceVideo() {
        String html = "<html><head><title>电影视频教程</title></head><body>" +
                "<p>高清视频下载 https://pan.baidu.com/s/1video</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.ResourceType.VIDEO, resources.get(0).getResourceType());
    }

    @Test
    void testResourceTypeInferenceDocument() {
        String html = "<html><head><title>电子书PDF教程</title></head><body>" +
                "<p>文档下载 https://pan.quark.cn/s/1doc</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.ResourceType.DOCUMENT, resources.get(0).getResourceType());
    }

    @Test
    void testResourceTypeInferenceSoftware() {
        String html = "<html><head><title>软件安装包工具</title></head><body>" +
                "<p>https://pan.baidu.com/s/1software</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.ResourceType.SOFTWARE, resources.get(0).getResourceType());
    }

    @Test
    void testBaiduPanWithPwdParam() {
        String html = "<html><head><title>带pwd参数</title></head><body>" +
                "<p>https://pan.baidu.com/s/1withpwd?pwd=abcd</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals("https://pan.baidu.com/s/1withpwd?pwd=abcd", resources.get(0).getPanUrl());
    }

    @Test
    void testUcPanLink() {
        String html = "<html><head><title>UC网盘</title></head><body>" +
                "<p>https://drive.uc.cn/s/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.UC, resources.get(0).getPanType());
    }

    @Test
    void test115PanLink() {
        String html = "<html><head><title>115网盘</title></head><body>" +
                "<p>https://115.com/s/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.PAN115, resources.get(0).getPanType());
    }

    @Test
    void test123PanLink() {
        String html = "<html><head><title>123网盘</title></head><body>" +
                "<p>https://www.123pan.com/s/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.PAN123, resources.get(0).getPanType());
    }

    @Test
    void testWeiyunLink() {
        String html = "<html><head><title>微云</title></head><body>" +
                "<p>https://share.weiyun.com/abc123</p>" +
                "</body></html>";
        Document doc = Jsoup.parse(html, "https://example.com");
        List<Resource> resources = pageParser.parsePage("https://example.com", doc);

        assertFalse(resources.isEmpty());
        assertEquals(Resource.PanType.WEIYUN, resources.get(0).getPanType());
    }
}
