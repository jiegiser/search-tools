package com.searchtools.tests;

import com.searchtools.model.Resource;
import com.searchtools.model.Resource.PanType;
import com.searchtools.model.Resource.ResourceType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Resource模型全面测试
 */
class ResourceModelTest {

    @Test
    void testResourceBuilder() {
        Resource resource = Resource.builder()
                .id(1L)
                .title("测试资源")
                .description("测试描述")
                .panUrl("https://pan.baidu.com/s/1test")
                .panType(PanType.BAIDU)
                .extractCode("abcd")
                .resourceType(ResourceType.DOCUMENT)
                .sourceUrl("https://example.com")
                .sourceSite("example.com")
                .fileSize("100MB")
                .isValid(true)
                .clickCount(50L)
                .build();

        assertEquals(1L, resource.getId());
        assertEquals("测试资源", resource.getTitle());
        assertEquals("测试描述", resource.getDescription());
        assertEquals("https://pan.baidu.com/s/1test", resource.getPanUrl());
        assertEquals(PanType.BAIDU, resource.getPanType());
        assertEquals("abcd", resource.getExtractCode());
        assertEquals(ResourceType.DOCUMENT, resource.getResourceType());
        assertEquals("https://example.com", resource.getSourceUrl());
        assertEquals("example.com", resource.getSourceSite());
        assertEquals("100MB", resource.getFileSize());
        assertTrue(resource.getIsValid());
        assertEquals(50L, resource.getClickCount());
    }

    @Test
    void testResourceDefaultValues() {
        Resource resource = new Resource();
        assertNull(resource.getId());
        assertNull(resource.getTitle());
        assertNull(resource.getPanUrl());
        assertTrue(resource.getIsValid());
        assertEquals(0L, resource.getClickCount());
    }

    @Test
    void testPanTypeDisplayName() {
        assertEquals("百度网盘", PanType.BAIDU.getDisplayName());
        assertEquals("阿里云盘", PanType.ALIYUN.getDisplayName());
        assertEquals("夸克网盘", PanType.QUARK.getDisplayName());
        assertEquals("迅雷网盘", PanType.XUNLEI.getDisplayName());
        assertEquals("天翼云盘", PanType.TIANYI.getDisplayName());
        assertEquals("UC网盘", PanType.UC.getDisplayName());
        assertEquals("115网盘", PanType.PAN115.getDisplayName());
        assertEquals("123网盘", PanType.PAN123.getDisplayName());
        assertEquals("微云", PanType.WEIYUN.getDisplayName());
        assertEquals("蓝奏云", PanType.LANZOU.getDisplayName());
        assertEquals("MEGA", PanType.MEGA.getDisplayName());
        assertEquals("其他", PanType.OTHER.getDisplayName());
    }

    @Test
    void testResourceTypeDisplayName() {
        assertEquals("视频", ResourceType.VIDEO.getDisplayName());
        assertEquals("文档", ResourceType.DOCUMENT.getDisplayName());
        assertEquals("软件", ResourceType.SOFTWARE.getDisplayName());
        assertEquals("音乐", ResourceType.MUSIC.getDisplayName());
        assertEquals("图片", ResourceType.IMAGE.getDisplayName());
        assertEquals("压缩包", ResourceType.ARCHIVE.getDisplayName());
        assertEquals("其他", ResourceType.OTHER.getDisplayName());
    }

    @Test
    void testPanTypeValues() {
        PanType[] values = PanType.values();
        assertTrue(values.length >= 7);
        assertNotNull(PanType.valueOf("BAIDU"));
        assertNotNull(PanType.valueOf("ALIYUN"));
        assertNotNull(PanType.valueOf("QUARK"));
    }

    @Test
    void testResourceTypeValues() {
        ResourceType[] values = ResourceType.values();
        assertEquals(7, values.length);
    }

    @Test
    void testResourceEquality() {
        Resource r1 = Resource.builder().id(1L).title("Test").panUrl("https://test.com").panType(PanType.BAIDU).build();
        Resource r2 = Resource.builder().id(1L).title("Test").panUrl("https://test.com").panType(PanType.BAIDU).build();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testResourceInequality() {
        Resource r1 = Resource.builder().id(1L).title("Test1").panUrl("https://test1.com").panType(PanType.BAIDU).build();
        Resource r2 = Resource.builder().id(2L).title("Test2").panUrl("https://test2.com").panType(PanType.ALIYUN).build();
        assertNotEquals(r1, r2);
    }

    @Test
    void testResourceToString() {
        Resource resource = Resource.builder()
                .id(1L)
                .title("Test Resource")
                .panUrl("https://pan.baidu.com/s/test")
                .panType(PanType.BAIDU)
                .build();
        String str = resource.toString();
        assertTrue(str.contains("Test Resource"));
        assertTrue(str.contains("BAIDU"));
    }

    @Test
    void testResourceWithNullOptionalFields() {
        Resource resource = Resource.builder()
                .title("Test")
                .panUrl("https://pan.quark.cn/s/test")
                .panType(PanType.QUARK)
                .build();

        assertNull(resource.getExtractCode());
        assertNull(resource.getResourceType());
        assertNull(resource.getSourceUrl());
        assertNull(resource.getSourceSite());
        assertNull(resource.getFileSize());
        assertNull(resource.getDescription());
    }

    @Test
    void testResourceClickCountIncrement() {
        Resource resource = Resource.builder()
                .title("Test")
                .panUrl("https://pan.baidu.com/s/test")
                .panType(PanType.BAIDU)
                .clickCount(0L)
                .build();

        resource.setClickCount(resource.getClickCount() + 1);
        assertEquals(1L, resource.getClickCount());

        resource.setClickCount(resource.getClickCount() + 1);
        assertEquals(2L, resource.getClickCount());
    }

    @Test
    void testResourceIsValidToggle() {
        Resource resource = Resource.builder()
                .title("Test")
                .panUrl("https://pan.baidu.com/s/test")
                .panType(PanType.BAIDU)
                .isValid(true)
                .build();

        assertTrue(resource.getIsValid());
        resource.setIsValid(false);
        assertFalse(resource.getIsValid());
    }
}
