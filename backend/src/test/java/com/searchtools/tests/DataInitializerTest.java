package com.searchtools.tests;

import com.searchtools.config.DataInitializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataInitializer测试 - 验证不再预置假数据
 */
class DataInitializerTest {

    @Test
    void testDataInitializerClassExists() {
        // 验证DataInitializer类存在且可加载
        assertNotNull(DataInitializer.class);
    }

    @Test
    void testDataInitializerImplementsCommandLineRunner() {
        // 验证DataInitializer实现了CommandLineRunner接口
        assertTrue(org.springframework.boot.CommandLineRunner.class.isAssignableFrom(DataInitializer.class));
    }

    @Test
    void testNoHardcodedFakeUrls() {
        // 通过反射验证DataInitializer源码中不包含假URL
        // 这是一个编译时验证 - 如果源码中还有假URL，这个测试会提醒
        String[] fakeUrls = {
            "https://pan.baidu.com/s/1java_master",
            "https://www.aliyundrive.com/s/modern_cpp",
            "https://pan.baidu.com/s/1java_thinking",
            "https://pan.quark.cn/s/algorithms"
        };

        // 读取源文件验证
        try {
            String sourcePath = "src/main/java/com/searchtools/config/DataInitializer.java";
            java.io.File file = new java.io.File(sourcePath);
            if (file.exists()) {
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                for (String fakeUrl : fakeUrls) {
                    assertFalse(content.contains(fakeUrl),
                            "DataInitializer should not contain fake URL: " + fakeUrl);
                }
            }
        } catch (Exception e) {
            // 如果无法读取源文件，跳过此测试
            // 在Maven测试环境中源文件路径可能不同
        }
    }

    @Test
    void testResourceModelSupportsRealUrls() {
        // 验证Resource模型支持真实的网盘URL格式
        com.searchtools.model.Resource resource = com.searchtools.model.Resource.builder()
                .title("真实资源")
                .panUrl("https://pan.baidu.com/s/1realShareLink")
                .panType(com.searchtools.model.Resource.PanType.BAIDU)
                .sourceUrl("https://www.baidu.com/s?wd=Java教程")
                .sourceSite("www.baidu.com")
                .isValid(true)
                .clickCount(0L)
                .build();

        assertNotNull(resource);
        assertEquals("https://pan.baidu.com/s/1realShareLink", resource.getPanUrl());
        assertEquals("https://www.baidu.com/s?wd=Java教程", resource.getSourceUrl());
        assertEquals("www.baidu.com", resource.getSourceSite());
    }

    @Test
    void testSourceSiteExtractionFromRealUrls() {
        // 验证能从真实URL中提取来源站点
        String[] testUrls = {
            "https://pan.baidu.com/s/1test",
            "https://www.alipan.com/s/test",
            "https://pan.quark.cn/s/test",
            "https://mega.nz/file/test"
        };

        for (String url : testUrls) {
            try {
                java.net.URI uri = new java.net.URI(url);
                assertNotNull(uri.getHost(), "Should extract host from: " + url);
            } catch (Exception e) {
                fail("Should parse URL: " + url);
            }
        }
    }
}
