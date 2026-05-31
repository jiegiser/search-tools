package com.searchtools.integration;

import com.searchtools.model.Resource;
import com.searchtools.service.LinkValidatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 链接验证服务测试 - 纯JUnit测试，不依赖Spring上下文
 */
class LinkValidatorServiceTest {

    @Test
    void testValidationResultBuilderValid() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(true)
                .message("链接有效")
                .needExtractCode(true)
                .build();

        assertTrue(result.isValid());
        assertEquals("链接有效", result.getMessage());
        assertTrue(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultBuilderInvalid() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(false)
                .message("链接已失效")
                .needExtractCode(false)
                .build();

        assertFalse(result.isValid());
        assertEquals("链接已失效", result.getMessage());
        assertFalse(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultNeedExtractCode() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(true)
                .message("链接有效（需要提取码）")
                .needExtractCode(true)
                .build();

        assertTrue(result.isValid());
        assertTrue(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultNoExtractCode() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(true)
                .message("链接有效")
                .needExtractCode(false)
                .build();

        assertTrue(result.isValid());
        assertFalse(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultNullMessage() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(false)
                .build();

        assertFalse(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    void testPanTypeValues() {
        // 验证所有网盘类型
        assertNotNull(Resource.PanType.BAIDU);
        assertNotNull(Resource.PanType.ALIYUN);
        assertNotNull(Resource.PanType.QUARK);
        assertNotNull(Resource.PanType.XUNLEI);
        assertNotNull(Resource.PanType.TIANYI);
        assertNotNull(Resource.PanType.UC);
        assertNotNull(Resource.PanType.PAN115);
        assertNotNull(Resource.PanType.PAN123);
        assertNotNull(Resource.PanType.WEIYUN);
        assertNotNull(Resource.PanType.LANZOU);
        assertNotNull(Resource.PanType.MEGA);
        assertNotNull(Resource.PanType.OTHER);
        assertEquals(12, Resource.PanType.values().length);
    }

    @Test
    void testResourceBuilder() {
        Resource resource = Resource.builder()
                .id(1L)
                .title("测试资源")
                .panUrl("https://pan.baidu.com/s/test")
                .panType(Resource.PanType.BAIDU)
                .extractCode("1234")
                .resourceType(Resource.ResourceType.VIDEO)
                .isValid(true)
                .clickCount(50L)
                .build();

        assertNotNull(resource);
        assertEquals(1L, resource.getId());
        assertEquals("测试资源", resource.getTitle());
        assertEquals("https://pan.baidu.com/s/test", resource.getPanUrl());
        assertEquals(Resource.PanType.BAIDU, resource.getPanType());
        assertEquals("1234", resource.getExtractCode());
        assertEquals(Resource.ResourceType.VIDEO, resource.getResourceType());
        assertTrue(resource.getIsValid());
        assertEquals(50L, resource.getClickCount());
    }
}
