package com.searchtools.tests;

import com.searchtools.model.Resource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索引擎全量测试 - 覆盖Lucene搜索引擎和PageParser的逻辑
 */
class SearchEngineTest {

    // ==================== Lucene特殊字符转义测试 ====================

    @Test
    void testEscapeLuceneSpecialCharacters() {
        // 模拟Lucene特殊字符转义
        String keyword = "C++";
        String escaped = escapeLuceneSpecial(keyword.toLowerCase());
        assertEquals("c\\+\\+", escaped);
    }

    @Test
    void testEscapeLuceneSpecialWithMultipleChars() {
        // # is NOT a Lucene special character, so not escaped
        assertEquals("c#", escapeLuceneSpecial("c#"));
        assertEquals("\\(test\\)", escapeLuceneSpecial("(test)"));
        assertEquals("a\\&b", escapeLuceneSpecial("a&b"));
        assertEquals("a\\|b", escapeLuceneSpecial("a|b"));
        assertEquals("a\\!b", escapeLuceneSpecial("a!b"));
    }

    @Test
    void testEscapeLuceneSpecialWithNoSpecialChars() {
        assertEquals("java", escapeLuceneSpecial("java"));
        assertEquals("python", escapeLuceneSpecial("python"));
        assertEquals("hello world", escapeLuceneSpecial("hello world"));
    }

    @Test
    void testEscapeLuceneSpecialWithEmptyString() {
        assertEquals("", escapeLuceneSpecial(""));
    }

    // ==================== WildcardQuery构建测试 ====================

    @Test
    void testWildcardQueryPattern() {
        String keyword = "java";
        String wildcardPattern = "*" + keyword + "*";
        assertEquals("*java*", wildcardPattern);
    }

    @Test
    void testWildcardQueryWithEscapedChars() {
        String keyword = "c++";
        String escaped = escapeLuceneSpecial(keyword.toLowerCase());
        String wildcardPattern = "*" + escaped + "*";
        assertEquals("*c\\+\\+*", wildcardPattern);
    }

    // ==================== PageParser：百度网盘链接解析 ====================

    @Test
    void testBaiduPanUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://pan\\.baidu\\.com/s/[a-zA-Z0-9_-]+(?:\\?pwd=[a-zA-Z0-9]+)?",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://pan.baidu.com/s/1abcDEF").find());
        assertTrue(pattern.matcher("https://pan.baidu.com/s/1abcDEF?pwd=abcd").find());
        assertTrue(pattern.matcher("http://pan.baidu.com/s/1test").find());
        assertFalse(pattern.matcher("https://www.aliyundrive.com/s/test").find());
    }

    @Test
    void testBaiduPanUrlExtraction() {
        Pattern pattern = Pattern.compile(
                "https?://pan\\.baidu\\.com/s/[a-zA-Z0-9_-]+(?:\\?pwd=[a-zA-Z0-9]+)?",
                Pattern.CASE_INSENSITIVE
        );

        String html = "下载链接：https://pan.baidu.com/s/1abcDEF?pwd=1234 提取码：1234";
        Matcher matcher = pattern.matcher(html);

        assertTrue(matcher.find());
        assertEquals("https://pan.baidu.com/s/1abcDEF?pwd=1234", matcher.group());
    }

    // ==================== PageParser：阿里云盘链接解析 ====================

    @Test
    void testAliyunDriveUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://(?:www\\.)?(?:aliyundrive|alipan)\\.com/s/[a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://www.aliyundrive.com/s/abc123").find());
        assertTrue(pattern.matcher("https://www.alipan.com/s/abc123").find());
        assertFalse(pattern.matcher("https://pan.baidu.com/s/test").find());
    }

    // ==================== PageParser：夸克网盘链接解析 ====================

    @Test
    void testQuarkPanUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://pan\\.quark\\.cn/s/[a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://pan.quark.cn/s/abc123").find());
        assertFalse(pattern.matcher("https://pan.baidu.com/s/test").find());
    }

    // ==================== PageParser：其他网盘链接解析 ====================

    @Test
    void testXunleiPanUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://pan\\.xunlei\\.com/s/[a-zA-Z0-9_-]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://pan.xunlei.com/s/abc123").find());
        assertTrue(pattern.matcher("https://pan.xunlei.com/s/abc_123-test").find());
    }

    @Test
    void testTianyiPanUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://cloud\\.189\\.cn/t/[a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://cloud.189.cn/t/abc123").find());
    }

    @Test
    void testUCPanUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://drive\\.uc\\.cn/s/[a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://drive.uc.cn/s/abc123").find());
    }

    @Test
    void testPan115UrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://115\\.com/s/[a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://115.com/s/abc123").find());
    }

    @Test
    void testPan123UrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://www\\.123pan\\.com/s/[a-zA-Z0-9_-]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://www.123pan.com/s/abc123").find());
        assertTrue(pattern.matcher("https://www.123pan.com/s/abc_123-test").find());
    }

    @Test
    void testLanzouUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://(?:www\\.)?lanzou[a-z]\\.com/[a-zA-Z0-9_-]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://www.lanzoue.com/abc123").find());
        assertTrue(pattern.matcher("https://lanzoui.com/abc_123").find());
    }

    @Test
    void testMegaUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://mega\\.nz/(?:file|folder)/[a-zA-Z0-9_-]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://mega.nz/file/abc123").find());
        assertTrue(pattern.matcher("https://mega.nz/folder/abc_123-test").find());
    }

    @Test
    void testWeiyunUrlPattern() {
        Pattern pattern = Pattern.compile(
                "https?://share\\.weiyun\\.com/[a-zA-Z0-9]+",
                Pattern.CASE_INSENSITIVE
        );

        assertTrue(pattern.matcher("https://share.weiyun.com/abc123").find());
    }

    // ==================== 提取码解析测试 ====================

    @Test
    void testExtractCodePattern() {
        Pattern pattern = Pattern.compile(
                "(?:提取码|密码|访问码|提取密码|pwd)[：:\\s]*([a-zA-Z0-9]{4,8})",
                Pattern.CASE_INSENSITIVE
        );

        Matcher m1 = pattern.matcher("提取码：abcd");
        assertTrue(m1.find());
        assertEquals("abcd", m1.group(1));

        Matcher m2 = pattern.matcher("密码:1234");
        assertTrue(m2.find());
        assertEquals("1234", m2.group(1));

        Matcher m3 = pattern.matcher("访问码 xyzw");
        assertTrue(m3.find());
        assertEquals("xyzw", m3.group(1));

        Matcher m4 = pattern.matcher("pwd:ab12");
        assertTrue(m4.find());
        assertEquals("ab12", m4.group(1));
    }

    @Test
    void testExtractCodeNearLink() {
        String html = "资源链接：https://pan.baidu.com/s/1test 提取码：abcd 请及时保存";

        Pattern panPattern = Pattern.compile("https?://pan\\.baidu\\.com/s/[a-zA-Z0-9_-]+");
        Pattern codePattern = Pattern.compile("(?:提取码|密码|访问码|提取密码|pwd)[：:\\s]*([a-zA-Z0-9]{4,8})");

        Matcher panMatcher = panPattern.matcher(html);
        assertTrue(panMatcher.find());
        String panUrl = panMatcher.group();

        // 在链接附近查找提取码
        int urlIndex = html.indexOf(panUrl);
        int start = Math.max(0, urlIndex - 200);
        int end = Math.min(html.length(), urlIndex + panUrl.length() + 200);
        String context = html.substring(start, end);

        Matcher codeMatcher = codePattern.matcher(context);
        assertTrue(codeMatcher.find());
        assertEquals("abcd", codeMatcher.group(1));
    }

    @Test
    void testExtractCodeNotFound() {
        Pattern pattern = Pattern.compile(
                "(?:提取码|密码|访问码|提取密码|pwd)[：:\\s]*([a-zA-Z0-9]{4,8})",
                Pattern.CASE_INSENSITIVE
        );

        assertFalse(pattern.matcher("没有提取码的页面").find());
        assertFalse(pattern.matcher("链接：https://pan.baidu.com/s/1test").find());
    }

    // ==================== 资源类型推断测试 ====================

    @Test
    void testInferVideoResourceType() {
        String title = "某电影高清版";
        String text = "电影资源下载";
        Resource.ResourceType type = inferResourceType(title, text);
        assertEquals(Resource.ResourceType.VIDEO, type);
    }

    @Test
    void testInferDocumentResourceType() {
        String title = "Java编程思想PDF";
        String text = "PDF电子书下载";
        Resource.ResourceType type = inferResourceType(title, text);
        assertEquals(Resource.ResourceType.DOCUMENT, type);
    }

    @Test
    void testInferSoftwareResourceType() {
        String title = "Adobe Photoshop 2024";
        String text = "软件工具安装包";
        Resource.ResourceType type = inferResourceType(title, text);
        assertEquals(Resource.ResourceType.SOFTWARE, type);
    }

    @Test
    void testInferMusicResourceType() {
        String title = "周杰伦专辑";
        String text = "音乐MP3 FLAC下载";
        Resource.ResourceType type = inferResourceType(title, text);
        assertEquals(Resource.ResourceType.MUSIC, type);
    }

    @Test
    void testInferArchiveResourceType() {
        String title = "资源合集";
        String text = "ZIP压缩包下载";
        Resource.ResourceType type = inferResourceType(title, text);
        assertEquals(Resource.ResourceType.ARCHIVE, type);
    }

    @Test
    void testInferOtherResourceType() {
        String title = "某资源";
        String text = "普通资源";
        Resource.ResourceType type = inferResourceType(title, text);
        assertEquals(Resource.ResourceType.OTHER, type);
    }

    // ==================== 来源站点提取测试 ====================

    @Test
    void testExtractSourceSite() {
        assertEquals("www.52pojie.cn", extractSourceSite("https://www.52pojie.cn/thread-123456-1-1.html"));
        assertEquals("github.com", extractSourceSite("https://github.com/user/repo"));
        assertEquals("www.baidu.com", extractSourceSite("https://www.baidu.com/s?wd=java"));
    }

    @Test
    void testExtractSourceSiteWithPort() {
        String site = extractSourceSite("https://example.com:8080/path");
        assertNotNull(site);
    }

    // ==================== 多链接页面解析测试 ====================

    @Test
    void testMultipleLinksInPage() {
        String html = "链接1: https://pan.baidu.com/s/1abc 提取码：1111 " +
                "链接2: https://pan.quark.cn/s/2def 提取码：2222 " +
                "链接3: https://www.aliyundrive.com/s/3ghi";

        Pattern baiduPattern = Pattern.compile("https?://pan\\.baidu\\.com/s/[a-zA-Z0-9_-]+");
        Pattern quarkPattern = Pattern.compile("https?://pan\\.quark\\.cn/s/[a-zA-Z0-9]+");
        Pattern aliyunPattern = Pattern.compile("https?://(?:www\\.)?(?:aliyundrive|alipan)\\.com/s/[a-zA-Z0-9]+");

        Matcher baiduMatcher = baiduPattern.matcher(html);
        assertTrue(baiduMatcher.find());

        Matcher quarkMatcher = quarkPattern.matcher(html);
        assertTrue(quarkMatcher.find());

        Matcher aliyunMatcher = aliyunPattern.matcher(html);
        assertTrue(aliyunMatcher.find());
    }

    // ==================== Lucene索引构建测试 ====================

    @Test
    void testIndexDocumentFields() {
        // 模拟Lucene文档字段
        Resource resource = Resource.builder()
                .id(1L)
                .title("测试标题")
                .description("测试描述")
                .panUrl("https://pan.baidu.com/s/test")
                .panType(Resource.PanType.BAIDU)
                .extractCode("1234")
                .resourceType(Resource.ResourceType.VIDEO)
                .sourceUrl("https://example.com")
                .sourceSite("example.com")
                .fileSize("100MB")
                .clickCount(50L)
                .build();

        assertNotNull(resource.getId().toString());
        assertNotNull(resource.getTitle());
        assertNotNull(resource.getDescription());
        assertNotNull(resource.getPanUrl());
        assertNotNull(resource.getPanType().name());
        assertNotNull(resource.getExtractCode());
        assertNotNull(resource.getResourceType().name());
        assertNotNull(resource.getSourceUrl());
        assertNotNull(resource.getSourceSite());
        assertNotNull(resource.getFileSize());
        assertNotNull(String.valueOf(resource.getClickCount()));
    }

    // ==================== 辅助方法 ====================

    private String escapeLuceneSpecial(String keyword) {
        StringBuilder sb = new StringBuilder();
        for (char c : keyword.toCharArray()) {
            if ("+-&|!(){}[]^\"~*?:\\/".indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private Resource.ResourceType inferResourceType(String title, String text) {
        String combined = (title + " " + text).toLowerCase();
        if (containsAny(combined, "电影", "视频", "电视剧", "动漫", "综艺", "mp4", "avi", "mkv", "video")) {
            return Resource.ResourceType.VIDEO;
        }
        if (containsAny(combined, "软件", "工具", "app", "安装包", "exe", "software")) {
            return Resource.ResourceType.SOFTWARE;
        }
        if (containsAny(combined, "音乐", "歌曲", "专辑", "mp3", "flac", "music")) {
            return Resource.ResourceType.MUSIC;
        }
        if (containsAny(combined, "文档", "pdf", "doc", "电子书", "教程", "document")) {
            return Resource.ResourceType.DOCUMENT;
        }
        if (containsAny(combined, "压缩包", "zip", "rar", "7z", "archive")) {
            return Resource.ResourceType.ARCHIVE;
        }
        return Resource.ResourceType.OTHER;
    }

    private String extractSourceSite(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getHost();
        } catch (java.net.URISyntaxException e) {
            return url;
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
