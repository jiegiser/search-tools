package com.searchtools.parser;

import com.searchtools.model.Resource;
import com.searchtools.model.Resource.PanType;
import com.searchtools.model.Resource.ResourceType;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网页解析器，提取网盘链接
 */
@Slf4j
@Component
public class PageParser {

    // 百度网盘链接正则
    private static final Pattern BAIDU_PAN_PATTERN = Pattern.compile(
            "https?://pan\\.baidu\\.com/s/[a-zA-Z0-9_-]+(?:\\?pwd=[a-zA-Z0-9]+)?",
            Pattern.CASE_INSENSITIVE
    );

    // 阿里云盘链接正则
    private static final Pattern ALIYUN_DRIVE_PATTERN = Pattern.compile(
            "https?://(?:www\\.)?(?:aliyundrive|alipan)\\.com/s/[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
    );

    // 夸克网盘链接正则
    private static final Pattern QUARK_PAN_PATTERN = Pattern.compile(
            "https?://pan\\.quark\\.cn/s/[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
    );

    // 迅雷网盘链接正则
    private static final Pattern XUNLEI_PAN_PATTERN = Pattern.compile(
            "https?://pan\\.xunlei\\.com/s/[a-zA-Z0-9_-]+",
            Pattern.CASE_INSENSITIVE
    );

    // 天翼云盘链接正则
    private static final Pattern TIANYI_PAN_PATTERN = Pattern.compile(
            "https?://cloud\\.189\\.cn/t/[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
    );

    // UC网盘链接正则
    private static final Pattern UC_PAN_PATTERN = Pattern.compile(
            "https?://drive\\.uc\\.cn/s/[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
    );

    // 115网盘链接正则
    private static final Pattern PAN115_PATTERN = Pattern.compile(
            "https?://115\\.com/s/[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
    );

    // 123网盘链接正则
    private static final Pattern PAN123_PATTERN = Pattern.compile(
            "https?://www\\.123pan\\.com/s/[a-zA-Z0-9_-]+",
            Pattern.CASE_INSENSITIVE
    );

    // 蓝奏云链接正则
    private static final Pattern LANZOU_PATTERN = Pattern.compile(
            "https?://(?:www\\.)?lanzou[a-z]\\.com/[a-zA-Z0-9_-]+",
            Pattern.CASE_INSENSITIVE
    );

    // MEGA链接正则
    private static final Pattern MEGA_PATTERN = Pattern.compile(
            "https?://mega\\.nz/(?:file|folder)/[a-zA-Z0-9_-]+",
            Pattern.CASE_INSENSITIVE
    );

    // 微云链接正则
    private static final Pattern WEIYUN_PATTERN = Pattern.compile(
            "https?://share\\.weiyun\\.com/[a-zA-Z0-9]+",
            Pattern.CASE_INSENSITIVE
    );

    // 提取码正则
    private static final Pattern EXTRACT_CODE_PATTERN = Pattern.compile(
            "(?:提取码|密码|访问码|提取密码|pwd)[：:\\s]*([a-zA-Z0-9]{4,8})",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析网页，提取网盘资源
     *
     * @param url      网页URL
     * @param document Jsoup文档对象
     * @return 资源列表
     */
    public List<Resource> parsePage(String url, Document document) {
        List<Resource> resources = new ArrayList<>();

        if (document == null) {
            return resources;
        }

        // 获取页面文本内容
        String pageText = document.text();
        // 获取页面HTML内容
        String pageHtml = document.html();

        // 提取标题
        String title = document.title();

        // 解析百度网盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, BAIDU_PAN_PATTERN, PanType.BAIDU));

        // 解析阿里云盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, ALIYUN_DRIVE_PATTERN, PanType.ALIYUN));

        // 解析夸克网盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, QUARK_PAN_PATTERN, PanType.QUARK));

        // 解析迅雷网盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, XUNLEI_PAN_PATTERN, PanType.XUNLEI));

        // 解析天翼云盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, TIANYI_PAN_PATTERN, PanType.TIANYI));

        // 解析UC网盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, UC_PAN_PATTERN, PanType.UC));

        // 解析115网盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, PAN115_PATTERN, PanType.PAN115));

        // 解析123网盘链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, PAN123_PATTERN, PanType.PAN123));

        // 解析蓝奏云链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, LANZOU_PATTERN, PanType.LANZOU));

        // 解析MEGA链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, MEGA_PATTERN, PanType.MEGA));

        // 解析微云链接
        resources.addAll(extractLinks(url, title, pageText, pageHtml, WEIYUN_PATTERN, PanType.WEIYUN));

        log.debug("从{}解析到{}个资源", url, resources.size());
        return resources;
    }

    /**
     * 提取指定类型的链接
     */
    private List<Resource> extractLinks(String sourceUrl, String pageTitle, String pageText,
                                         String pageHtml, Pattern pattern, PanType panType) {
        List<Resource> resources = new ArrayList<>();
        Matcher matcher = pattern.matcher(pageHtml);

        while (matcher.find()) {
            String panUrl = matcher.group();

            // 提取提取码
            String extractCode = extractCode(pageHtml, panUrl);

            // 推断资源类型
            ResourceType resourceType = inferResourceType(pageTitle, pageText);

            // 推断资源标题
            String resourceTitle = inferResourceTitle(pageTitle, pageText, panUrl);

            // 获取来源站点
            String sourceSite = extractSourceSite(sourceUrl);

            Resource resource = Resource.builder()
                    .title(resourceTitle)
                    .description(pageTitle)
                    .panUrl(panUrl)
                    .panType(panType)
                    .extractCode(extractCode)
                    .resourceType(resourceType)
                    .sourceUrl(sourceUrl)
                    .sourceSite(sourceSite)
                    .isValid(true)
                    .clickCount(0L)
                    .build();

            resources.add(resource);
        }

        return resources;
    }

    /**
     * 提取提取码
     */
    private String extractCode(String html, String panUrl) {
        // 在链接附近查找提取码
        int urlIndex = html.indexOf(panUrl);
        if (urlIndex > 0) {
            // 在链接前后500字符范围内查找
            int start = Math.max(0, urlIndex - 200);
            int end = Math.min(html.length(), urlIndex + panUrl.length() + 200);
            String context = html.substring(start, end);

            Matcher matcher = EXTRACT_CODE_PATTERN.matcher(context);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        // 在整个页面查找
        Matcher matcher = EXTRACT_CODE_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 推断资源类型
     */
    private ResourceType inferResourceType(String title, String text) {
        String combined = (title + " " + text).toLowerCase();

        if (containsAny(combined, "电影", "视频", "电视剧", "动漫", "综艺", "mp4", "avi", "mkv", "video")) {
            return ResourceType.VIDEO;
        }
        if (containsAny(combined, "软件", "工具", "app", "安装包", "exe", "software")) {
            return ResourceType.SOFTWARE;
        }
        if (containsAny(combined, "音乐", "歌曲", "专辑", "mp3", "flac", "music")) {
            return ResourceType.MUSIC;
        }
        if (containsAny(combined, "文档", "pdf", "doc", "电子书", "教程", "document")) {
            return ResourceType.DOCUMENT;
        }
        if (containsAny(combined, "图片", "壁纸", "素材", "image", "photo")) {
            return ResourceType.IMAGE;
        }
        if (containsAny(combined, "压缩包", "zip", "rar", "7z", "archive")) {
            return ResourceType.ARCHIVE;
        }

        return ResourceType.OTHER;
    }

    /**
     * 推断资源标题
     */
    private String inferResourceTitle(String pageTitle, String pageText, String panUrl) {
        // 尝试从页面标题中提取
        if (pageTitle != null && !pageTitle.isEmpty()) {
            // 清理标题
            String title = pageTitle
                    .replaceAll("\\s*[-_|].*$", "")  // 移除后面的分隔符
                    .replaceAll("\\s+", " ")
                    .trim();

            if (title.length() > 5 && title.length() < 200) {
                return title;
            }
        }

        // 使用网盘链接作为标题
        return panUrl;
    }

    /**
     * 提取来源站点
     */
    private String extractSourceSite(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return url;
        }
    }

    /**
     * 检查字符串是否包含任一关键词
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
