package com.searchtools.service;

import com.searchtools.model.Resource;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 网盘链接验证服务 - 验证网盘链接是否可以打开
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LinkValidatorService {

    private final ResourceRepository resourceRepository;

    @Value("${crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    /**
     * 验证单个网盘链接是否有效
     *
     * @param panUrl  网盘链接
     * @param panType 网盘类型
     * @return 验证结果
     */
    public ValidationResult validateLink(String panUrl, Resource.PanType panType) {
        log.info("验证链接: {} ({})", panUrl, panType);

        try {
            // 根据网盘类型使用不同的验证策略
            return switch (panType) {
                case BAIDU -> validateBaiduPan(panUrl);
                case ALIYUN -> validateAliyunDrive(panUrl);
                case QUARK -> validateQuarkPan(panUrl);
                default -> validateGenericLink(panUrl);
            };
        } catch (Exception e) {
            log.error("验证链接失败: {}", panUrl, e);
            return ValidationResult.builder()
                    .valid(false)
                    .message("验证失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 验证百度网盘链接
     */
    private ValidationResult validateBaiduPan(String panUrl) {
        try {
            Document doc = Jsoup.connect(panUrl)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            // 检查页面是否包含失效提示
            String pageText = doc.text();
            if (pageText.contains("分享的文件已经被取消") ||
                pageText.contains("分享已过期") ||
                pageText.contains("页面不存在") ||
                pageText.contains("链接错误")) {
                return ValidationResult.builder()
                        .valid(false)
                        .message("链接已失效")
                        .build();
            }

            // 检查是否有提取码输入框（说明链接有效但需要提取码）
            boolean hasExtractCodeInput = doc.select("input#accessCode").size() > 0;

            return ValidationResult.builder()
                    .valid(true)
                    .message(hasExtractCodeInput ? "链接有效（需要提取码）" : "链接有效")
                    .needExtractCode(hasExtractCodeInput)
                    .build();
        } catch (IOException e) {
            return ValidationResult.builder()
                    .valid(false)
                    .message("无法访问: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 验证阿里云盘链接
     */
    private ValidationResult validateAliyunDrive(String panUrl) {
        try {
            Document doc = Jsoup.connect(panUrl)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            String pageText = doc.text();
            if (pageText.contains("分享已失效") ||
                pageText.contains("页面不存在") ||
                pageText.contains("链接已失效")) {
                return ValidationResult.builder()
                        .valid(false)
                        .message("链接已失效")
                        .build();
            }

            return ValidationResult.builder()
                    .valid(true)
                    .message("链接有效")
                    .build();
        } catch (IOException e) {
            return ValidationResult.builder()
                    .valid(false)
                    .message("无法访问: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 验证夸克网盘链接
     */
    private ValidationResult validateQuarkPan(String panUrl) {
        try {
            Document doc = Jsoup.connect(panUrl)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            String pageText = doc.text();
            if (pageText.contains("分享已失效") ||
                pageText.contains("页面不存在") ||
                pageText.contains("链接已过期")) {
                return ValidationResult.builder()
                        .valid(false)
                        .message("链接已失效")
                        .build();
            }

            return ValidationResult.builder()
                    .valid(true)
                    .message("链接有效")
                    .build();
        } catch (IOException e) {
            return ValidationResult.builder()
                    .valid(false)
                    .message("无法访问: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 通用链接验证（通过HTTP状态码判断）
     */
    private ValidationResult validateGenericLink(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 400) {
                return ValidationResult.builder()
                        .valid(true)
                        .message("链接有效 (HTTP " + responseCode + ")")
                        .build();
            } else {
                return ValidationResult.builder()
                        .valid(false)
                        .message("链接无效 (HTTP " + responseCode + ")")
                        .build();
            }
        } catch (IOException e) {
            return ValidationResult.builder()
                    .valid(false)
                    .message("无法访问: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 批量验证资源链接
     *
     * @param resources 资源列表
     * @return 验证结果数量
     */
    public int validateBatch(List<Resource> resources) {
        int validCount = 0;
        for (Resource resource : resources) {
            ValidationResult result = validateLink(resource.getPanUrl(), resource.getPanType());
            resource.setIsValid(result.isValid());
            resourceRepository.save(resource);
            if (result.isValid()) {
                validCount++;
            }
        }
        return validCount;
    }

    /**
     * 异步验证链接
     *
     * @param panUrl  网盘链接
     * @param panType 网盘类型
     * @return CompletableFuture
     */
    @Async
    public CompletableFuture<ValidationResult> validateLinkAsync(String panUrl, Resource.PanType panType) {
        return CompletableFuture.completedFuture(validateLink(panUrl, panType));
    }

    /**
     * 验证结果
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private boolean needExtractCode;
    }
}
