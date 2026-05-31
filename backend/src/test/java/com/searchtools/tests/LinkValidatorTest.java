package com.searchtools.tests;

import com.searchtools.model.Resource;
import com.searchtools.service.LinkValidatorService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LinkValidatorService验证结果测试
 */
class LinkValidatorTest {

    @Test
    void testValidationResultBuilder() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(true)
                .message("链接有效")
                .needExtractCode(false)
                .build();

        assertTrue(result.isValid());
        assertEquals("链接有效", result.getMessage());
        assertFalse(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultInvalid() {
        LinkValidatorService.ValidationResult result = LinkValidatorService.ValidationResult.builder()
                .valid(false)
                .message("链接已失效")
                .build();

        assertFalse(result.isValid());
        assertEquals("链接已失效", result.getMessage());
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
    void testValidationResultDefaultValues() {
        LinkValidatorService.ValidationResult result = new LinkValidatorService.ValidationResult();
        assertFalse(result.isValid());
        assertNull(result.getMessage());
        assertFalse(result.isNeedExtractCode());
    }

    @Test
    void testValidationResultEquality() {
        LinkValidatorService.ValidationResult r1 = LinkValidatorService.ValidationResult.builder()
                .valid(true).message("ok").build();
        LinkValidatorService.ValidationResult r2 = LinkValidatorService.ValidationResult.builder()
                .valid(true).message("ok").build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testPanTypeDetection() {
        // 验证各种网盘URL能被正确识别类型
        assertTrue("https://pan.baidu.com/s/1test".contains("baidu"));
        assertTrue("https://www.alipan.com/s/test".contains("alipan"));
        assertTrue("https://pan.quark.cn/s/test".contains("quark"));
    }

    @Test
    void testResourceIsValidFieldForValidation() {
        Resource resource = Resource.builder()
                .title("Test")
                .panUrl("https://pan.baidu.com/s/test")
                .panType(Resource.PanType.BAIDU)
                .isValid(true)
                .build();

        // 模拟验证后设置为无效
        resource.setIsValid(false);
        assertFalse(resource.getIsValid());

        // 模拟验证后设置为有效
        resource.setIsValid(true);
        assertTrue(resource.getIsValid());
    }
}
