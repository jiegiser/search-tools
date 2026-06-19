package com.searchtools.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchtools.model.Resource;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网盘资源聚合API客户端
 * 集成多个开源网盘资源搜索API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PanResourceApiClient {

    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${pansou.api.enabled:true}")
    private boolean enabled;
    
    @Value("${pansou.api.url:https://pansou.862812.xyz}")
    private String pansouApiUrl;
    
    @Value("${pansou.api.timeout:10000}")
    private int timeout;
    
    // 网盘链接正则表达式
    private static final Pattern BAIDU_PAN_PATTERN = Pattern.compile("https?://pan\\.baidu\\.com/s/[\\w-]+");
    private static final Pattern ALIYUN_PAN_PATTERN = Pattern.compile("https?://www\\.aliyundrive\\.com/s/[\\w-]+|https?://www\\.alipan\\.com/s/[\\w-]+");
    private static final Pattern QUARK_PAN_PATTERN = Pattern.compile("https?://pan\\.quark\\.cn/s/[\\w-]+");
    private static final Pattern PAN115_PATTERN = Pattern.compile("https?://115\\.com/s/[\\w-]+");
    private static final Pattern XUNLEI_PAN_PATTERN = Pattern.compile("https?://pan\\.xunlei\\.com/s/[\\w-]+");
    
    // 提取码正则
    private static final Pattern EXTRACT_CODE_PATTERN = Pattern.compile("(?:提取码|密码|提取密码|访问码)[：:\\s]*([a-zA-Z0-9]{4,8})");
    
    /**
     * 搜索网盘资源
     * 
     * @param keyword 搜索关键词
     * @return 资源列表
     */
    public List<Resource> searchResources(String keyword) {
        List<Resource> allResources = new ArrayList<>();
        
        if (!enabled) {
            log.info("网盘资源API未启用");
            return allResources;
        }
        
        log.info("开始搜索网盘资源: {}", keyword);
        
        // 尝试从多个API源获取资源
        allResources.addAll(searchFromPansou(keyword));
        allResources.addAll(searchFromPanhub(keyword));
        
        log.info("网盘资源搜索完成: {}, 共找到{}个资源", keyword, allResources.size());
        return allResources;
    }
    
    /**
     * 从PanSou API搜索资源
     * PanSou是一个开源的高性能网盘资源搜索API服务
     * 项目地址: https://github.com/fish2018/pansou
     */
    private List<Resource> searchFromPansou(String keyword) {
        List<Resource> resources = new ArrayList<>();
        
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
            
            // PanSou API接口
            String apiUrl = String.format("%s/api/search?keyword=%s&timeout=%d", 
                pansouApiUrl, 
                java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8),
                timeout);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "SearchTools/1.0")
                .header("Accept", "application/json")
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                resources = parsePansouResponse(response.body());
                log.info("PanSou API返回{}个资源", resources.size());
            } else {
                log.warn("PanSou API请求失败: {}", response.statusCode());
            }
            
        } catch (Exception e) {
            log.warn("PanSou API请求异常: {}", e.getMessage());
        }
        
        return resources;
    }
    
    /**
     * 解析PanSou API响应
     */
    private List<Resource> parsePansouResponse(String responseBody) {
        List<Resource> resources = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.get("data");
            
            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    try {
                        Resource resource = new Resource();
                        
                        // 标题
                        String title = item.has("name") ? item.get("name").asText() : "未知资源";
                        resource.setTitle(title);
                        
                        // 网盘链接
                        String url = item.has("url") ? item.get("url").asText() : "";
                        if (url.isEmpty()) {
                            continue;
                        }
                        resource.setPanUrl(url);
                        
                        // 网盘类型
                        String panType = item.has("type") ? item.get("type").asText() : "";
                        resource.setPanType(detectPanType(url, panType));
                        
                        // 提取码
                        String extractCode = item.has("pwd") ? item.get("pwd").asText() : "";
                        resource.setExtractCode(extractCode.isEmpty() ? null : extractCode);
                        
                        // 来源
                        resource.setSourceUrl(item.has("from") ? item.get("from").asText() : "pansou");
                        resource.setSourceSite("pansou");
                        
                        // 其他信息
                        resource.setResourceType(Resource.ResourceType.OTHER);
                        resource.setIsValid(true);
                        resource.setClickCount(0L);
                        
                        // 检查是否已存在
                        List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                        if (existing.isEmpty()) {
                            resourceRepository.save(resource);
                            resources.add(resource);
                        }
                        
                    } catch (Exception e) {
                        log.debug("解析资源项失败: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("解析PanSou响应失败: {}", e.getMessage());
        }
        
        return resources;
    }
    
    /**
     * 从Panhub搜索资源（备用API）
     */
    private List<Resource> searchFromPanhub(String keyword) {
        List<Resource> resources = new ArrayList<>();
        
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
            
            // Panhub API接口（示例）
            String apiUrl = "https://panhub.org/api/search";
            String requestBody = String.format("{\"keyword\":\"%s\",\"page\":1,\"pageSize\":20}", keyword);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("User-Agent", "SearchTools/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 00) {
                resources = parseGenericPanResponse(response.body(), "panhub");
                log.info("Panhub API返回{}个资源", resources.size());
            }
            
        } catch (Exception e) {
            log.debug("Panhub API请求异常: {}", e.getMessage());
        }
        
        return resources;
    }
    
    /**
     * 解析通用网盘资源响应
     */
    private List<Resource> parseGenericPanResponse(String responseBody, String source) {
        List<Resource> resources = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.has("data") ? root.get("data") : root.has("list") ? root.get("list") : root;
            
            if (data.isArray()) {
                for (JsonNode item : data) {
                    try {
                        Resource resource = new Resource();
                        resource.setTitle(item.has("title") ? item.get("title").asText() : 
                                         item.has("name") ? item.get("name").asText() : "未知资源");
                        
                        String url = item.has("url") ? item.get("url").asText() : 
                                   item.has("link") ? item.get("link").asText() : "";
                        if (url.isEmpty()) continue;
                        
                        resource.setPanUrl(url);
                        resource.setPanType(detectPanType(url, ""));
                        resource.setExtractCode(item.has("password") ? item.get("password").asText() : null);
                        resource.setSourceSite(source);
                        resource.setResourceType(Resource.ResourceType.OTHER);
                        resource.setIsValid(true);
                        resource.setClickCount(0L);
                        
                        List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                        if (existing.isEmpty()) {
                            resourceRepository.save(resource);
                            resources.add(resource);
                        }
                        
                    } catch (Exception e) {
                        log.debug("解析资源项失败: {}", e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.debug("解析{}响应失败: {}", source, e.getMessage());
        }
        
        return resources;
    }
    
    /**
     * 检测网盘类型
     */
    private Resource.PanType detectPanType(String url, String type) {
        if (url == null) return Resource.PanType.OTHER;
        
        String lowerUrl = url.toLowerCase();
        
        if (lowerUrl.contains("pan.baidu.com") || lowerUrl.contains("yun.baidu.com")) {
            return Resource.PanType.BAIDU;
        } else if (lowerUrl.contains("aliyundrive.com") || lowerUrl.contains("alipan.com")) {
            return Resource.PanType.ALIYUN;
        } else if (lowerUrl.contains("pan.quark.cn")) {
            return Resource.PanType.QUARK;
        } else if (lowerUrl.contains("115.com")) {
            return Resource.PanType.PAN115;
        } else if (lowerUrl.contains("pan.xunlei.com")) {
            return Resource.PanType.XUNLEI;
        } else if (lowerUrl.contains("lanzou") || lowerUrl.contains("lanzoui")) {
            return Resource.PanType.LANZOU;
        }
        
        // 根据type参数判断
        if (type != null) {
            String lowerType = type.toLowerCase();
            if (lowerType.contains("baidu")) return Resource.PanType.BAIDU;
            if (lowerType.contains("ali") || lowerType.contains("alipan")) return Resource.PanType.ALIYUN;
            if (lowerType.contains("quark")) return Resource.PanType.QUARK;
            if (lowerType.contains("115")) return Resource.PanType.PAN115;
            if (lowerType.contains("xunlei")) return Resource.PanType.XUNLEI;
        }
        
        return Resource.PanType.OTHER;
    }
    
    /**
     * 从网页内容中提取网盘链接
     */
    public List<String> extractPanLinks(String content) {
        List<String> links = new ArrayList<>();
        
        // 提取百度网盘链接
        Matcher matcher = BAIDU_PAN_PATTERN.matcher(content);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        
        // 提取阿里云盘链接
        matcher = ALIYUN_PAN_PATTERN.matcher(content);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        
        // 提取夸克网盘链接
        matcher = QUARK_PAN_PATTERN.matcher(content);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        
        // 提取115网盘链接
        matcher = PAN115_PATTERN.matcher(content);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        
        // 提取迅雷网盘链接
        matcher = XUNLEI_PAN_PATTERN.matcher(content);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        
        return links;
    }
    
    /**
     * 从内容中提取提取码
     */
    public String extractCode(String content) {
        Matcher matcher = EXTRACT_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
