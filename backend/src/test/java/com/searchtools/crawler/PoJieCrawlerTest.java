package com.searchtools.crawler;

import com.searchtools.model.PoJieResource;
import com.searchtools.model.Resource;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 52pojie爬虫测试
 */
class PoJieCrawlerTest {

    @Test
    void testPoJieResourceBuilder() {
        PoJieResource resource = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子标题")
                .threadUrl("https://www.52pojie.cn/thread-12345-1-1.html")
                .threadId("12345")
                .resourceUrl("https://pan.baidu.com/s/1abc123")
                .extractCode("abcd")
                .panType(Resource.PanType.BAIDU)
                .description("这是一个测试资源")
                .author("testUser")
                .matchedKeywords("Java,书")
                .isBestAnswer(true)
                .notified(false)
                .createdAt(LocalDateTime.now())
                .build();

        assertNotNull(resource);
        assertEquals(1L, resource.getId());
        assertEquals("测试帖子标题", resource.getThreadTitle());
        assertEquals("https://www.52pojie.cn/thread-12345-1-1.html", resource.getThreadUrl());
        assertEquals("12345", resource.getThreadId());
        assertEquals("https://pan.baidu.com/s/1abc123", resource.getResourceUrl());
        assertEquals("abcd", resource.getExtractCode());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals("这是一个测试资源", resource.getDescription());
        assertEquals("testUser", resource.getAuthor());
        assertEquals("Java,书", resource.getMatchedKeywords());
        assertTrue(resource.getIsBestAnswer());
        assertFalse(resource.getNotified());
        assertNotNull(resource.getCreatedAt());
    }

    @Test
    void testPoJieResourceDefaultValues() {
        PoJieResource resource = new PoJieResource();
        
        assertNull(resource.getId());
        assertNull(resource.getThreadTitle());
        assertNull(resource.getThreadUrl());
        assertNull(resource.getThreadId());
        assertNull(resource.getResourceUrl());
        assertNull(resource.getExtractCode());
        assertNull(resource.getPanType());
        assertNull(resource.getDescription());
        assertNull(resource.getAuthor());
        assertNull(resource.getMatchedKeywords());
        assertNull(resource.getCreatedAt());
        assertNull(resource.getNotifiedAt());
    }

    @Test
    void testPoJieResourceToString() {
        PoJieResource resource = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子")
                .panType(Resource.PanType.BAIDU)
                .build();

        String toString = resource.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("测试帖子"));
        assertTrue(toString.contains("BAIDU"));
    }

    @Test
    void testPoJieResourceEquality() {
        LocalDateTime now = LocalDateTime.now();
        
        PoJieResource resource1 = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子")
                .threadUrl("https://www.52pojie.cn/thread-12345-1-1.html")
                .resourceUrl("https://pan.baidu.com/s/1abc123")
                .panType(Resource.PanType.BAIDU)
                .createdAt(now)
                .build();

        PoJieResource resource2 = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子")
                .threadUrl("https://www.52pojie.cn/thread-12345-1-1.html")
                .resourceUrl("https://pan.baidu.com/s/1abc123")
                .panType(Resource.PanType.BAIDU)
                .createdAt(now)
                .build();

        assertEquals(resource1, resource2);
        assertEquals(resource1.hashCode(), resource2.hashCode());
    }

    @Test
    void testPoJieResourceInequality() {
        PoJieResource resource1 = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子1")
                .build();

        PoJieResource resource2 = PoJieResource.builder()
                .id(2L)
                .threadTitle("测试帖子2")
                .build();

        assertNotEquals(resource1, resource2);
    }

    @Test
    void testResourcePanTypeValues() {
        assertTrue(Resource.PanType.values().length >= 7);
        assertNotNull(Resource.PanType.BAIDU);
        assertNotNull(Resource.PanType.LANZOU);
        assertNotNull(Resource.PanType.ALIYUN);
        assertNotNull(Resource.PanType.QUARK);
        assertNotNull(Resource.PanType.XUNLEI);
        assertNotNull(Resource.PanType.TIANYI);
        assertNotNull(Resource.PanType.OTHER);
    }

    @Test
    void testPoJieResourceNotifiedStatus() {
        PoJieResource resource = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子")
                .notified(false)
                .build();

        assertFalse(resource.getNotified());
        
        resource.setNotified(true);
        resource.setNotifiedAt(LocalDateTime.now());
        
        assertTrue(resource.getNotified());
        assertNotNull(resource.getNotifiedAt());
    }

    @Test
    void testPoJieResourceBestAnswer() {
        PoJieResource resource = PoJieResource.builder()
                .id(1L)
                .threadTitle("测试帖子")
                .isBestAnswer(false)
                .build();

        assertFalse(resource.getIsBestAnswer());
        
        resource.setIsBestAnswer(true);
        assertTrue(resource.getIsBestAnswer());
    }

    @Test
    void testMultipleKeywords() {
        String keywords = "Java,C++,前端,架构师,书,电子书,PDF,教程";
        List<String> keywordList = Arrays.asList(keywords.split(","));
        
        assertEquals(8, keywordList.size());
        assertTrue(keywordList.contains("Java"));
        assertTrue(keywordList.contains("C++"));
        assertTrue(keywordList.contains("前端"));
        assertTrue(keywordList.contains("架构师"));
        assertTrue(keywordList.contains("书"));
        assertTrue(keywordList.contains("电子书"));
        assertTrue(keywordList.contains("PDF"));
        assertTrue(keywordList.contains("教程"));
    }
}
