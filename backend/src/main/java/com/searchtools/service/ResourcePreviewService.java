package com.searchtools.service;

import com.searchtools.model.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 资源预览服务
 * 支持预览网盘资源的基本信息
 */
@Slf4j
@Service
public class ResourcePreviewService {

    @Value("${crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    /**
     * 预览结果
     */
    public static class PreviewResult {
        private String title;
        private String description;
        private String thumbnail;
        private String fileSize;
        private String fileType;
        private boolean valid;
        private String errorMessage;
        private Map<String, String> extraInfo;

        public PreviewResult() {
            this.extraInfo = new HashMap<>();
        }

        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

        public String getFileSize() { return fileSize; }
        public void setFileSize(String fileSize) { this.fileSize = fileSize; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Map<String, String> getExtraInfo() { return extraInfo; }
        public void setExtraInfo(Map<String, String> extraInfo) { this.extraInfo = extraInfo; }
    }

    /**
     * 预览资源
     *
     * @param resource 资源对象
     * @return 预览结果
     */
    public PreviewResult preview(Resource resource) {
        if (resource == null || resource.getPanUrl() == null) {
            PreviewResult result = new PreviewResult();
            result.setValid(false);
            result.setErrorMessage("资源不存在");
            return result;
        }

        Resource.PanType panType = resource.getPanType();
        switch (panType) {
            case BAIDU:
                return previewBaiduPan(resource);
            case ALIYUN:
                return previewAliyunDrive(resource);
            case QUARK:
                return previewQuarkPan(resource);
            default:
                return previewGeneric(resource);
        }
    }

    /**
     * 预览百度网盘
     */
    private PreviewResult previewBaiduPan(Resource resource) {
        PreviewResult result = new PreviewResult();
        result.setTitle(resource.getTitle());
        result.setDescription(resource.getDescription());
        result.setFileType("百度网盘分享");

        try {
            Document doc = Jsoup.connect(resource.getPanUrl())
                    .userAgent(userAgent)
                    .timeout(10000)
                    .get();

            // 尝试提取标题
            String pageTitle = doc.title();
            if (pageTitle != null && !pageTitle.isEmpty() && !pageTitle.contains("百度网盘")) {
                result.setTitle(pageTitle);
            }

            // 检查链接是否有效
            String bodyText = doc.text();
            if (bodyText.contains("分享已过期") || bodyText.contains("页面不存在") ||
                bodyText.contains("访问错误")) {
                result.setValid(false);
                result.setErrorMessage("链接已失效");
            } else {
                result.setValid(true);
                // 提取文件信息
                extractFileInfo(bodyText, result);
            }

            // 尝试提取缩略图
            String ogImage = doc.select("meta[property=og:image]").attr("content");
            if (!ogImage.isEmpty()) {
                result.setThumbnail(ogImage);
            }

        } catch (IOException e) {
            log.warn("预览百度网盘失败: {}", e.getMessage());
            result.setValid(true); // 无法判断，假设有效
            result.getExtraInfo().put("note", "无法获取详细预览");
        }

        return result;
    }

    /**
     * 预览阿里云盘
     */
    private PreviewResult previewAliyunDrive(Resource resource) {
        PreviewResult result = new PreviewResult();
        result.setTitle(resource.getTitle());
        result.setDescription(resource.getDescription());
        result.setFileType("阿里云盘分享");

        try {
            Document doc = Jsoup.connect(resource.getPanUrl())
                    .userAgent(userAgent)
                    .timeout(10000)
                    .get();

            String pageTitle = doc.title();
            if (pageTitle != null && !pageTitle.isEmpty()) {
                result.setTitle(pageTitle);
            }

            String bodyText = doc.text();
            if (bodyText.contains("分享已失效") || bodyText.contains("页面不存在")) {
                result.setValid(false);
                result.setErrorMessage("链接已失效");
            } else {
                result.setValid(true);
            }

            // 尝试提取文件列表信息
            extractFileInfo(bodyText, result);

        } catch (IOException e) {
            log.warn("预览阿里云盘失败: {}", e.getMessage());
            result.setValid(true);
        }

        return result;
    }

    /**
     * 预览夸克网盘
     */
    private PreviewResult previewQuarkPan(Resource resource) {
        PreviewResult result = new PreviewResult();
        result.setTitle(resource.getTitle());
        result.setDescription(resource.getDescription());
        result.setFileType("夸克网盘分享");

        try {
            Document doc = Jsoup.connect(resource.getPanUrl())
                    .userAgent(userAgent)
                    .timeout(10000)
                    .get();

            String pageTitle = doc.title();
            if (pageTitle != null && !pageTitle.isEmpty()) {
                result.setTitle(pageTitle);
            }

            String bodyText = doc.text();
            if (bodyText.contains("分享已失效") || bodyText.contains("不存在")) {
                result.setValid(false);
                result.setErrorMessage("链接已失效");
            } else {
                result.setValid(true);
            }

            extractFileInfo(bodyText, result);

        } catch (IOException e) {
            log.warn("预览夸克网盘失败: {}", e.getMessage());
            result.setValid(true);
        }

        return result;
    }

    /**
     * 通用预览
     */
    private PreviewResult previewGeneric(Resource resource) {
        PreviewResult result = new PreviewResult();
        result.setTitle(resource.getTitle());
        result.setDescription(resource.getDescription());
        result.setFileType(resource.getPanType().getDisplayName());
        result.setValid(resource.getIsValid());

        if (resource.getExtractCode() != null && !resource.getExtractCode().isEmpty()) {
            result.getExtraInfo().put("extractCode", resource.getExtractCode());
        }

        return result;
    }

    /**
     * 提取文件信息
     */
    private void extractFileInfo(String text, PreviewResult result) {
        // 尝试匹配文件大小
        Pattern sizePattern = Pattern.compile("(\\d+(?:\\.\\d+)?\\s*(?:GB|MB|KB|TB))", Pattern.CASE_INSENSITIVE);
        Matcher sizeMatcher = sizePattern.matcher(text);
        if (sizeMatcher.find()) {
            result.setFileSize(sizeMatcher.group(1));
        }

        // 尝试提取文件数量
        Pattern countPattern = Pattern.compile("(\\d+)\\s*个文件");
        Matcher countMatcher = countPattern.matcher(text);
        if (countMatcher.find()) {
            result.getExtraInfo().put("fileCount", countMatcher.group(1) + "个文件");
        }
    }

    /**
     * 验证链接有效性
     *
     * @param url 网盘链接
     * @return 是否有效
     */
    public boolean validateLink(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .get();

            String bodyText = doc.text();
            return !bodyText.contains("分享已过期") &&
                   !bodyText.contains("分享已失效") &&
                   !bodyText.contains("页面不存在") &&
                   !bodyText.contains("访问错误") &&
                   !bodyText.contains("链接错误");
        } catch (IOException e) {
            log.warn("验证链接失败: {}", e.getMessage());
            return true; // 无法验证时假设有效
        }
    }
}
