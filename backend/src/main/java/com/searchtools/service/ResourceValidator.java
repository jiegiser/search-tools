package com.searchtools.service;

import com.searchtools.model.Resource;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * 资源可用性验证服务
 * 检查网盘链接是否可用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceValidator {

    private final ResourceRepository resourceRepository;

    /**
     * 验证单个资源的可用性
     */
    public boolean validateResource(Resource resource) {
        if (resource == null || resource.getPanUrl() == null || resource.getPanUrl().isEmpty()) {
            return false;
        }

        try {
            URL url = new URI(resource.getPanUrl()).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // 设置User-Agent模拟浏览器
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();

            // 2xx或3xx状态码表示链接可用
            boolean isValid = responseCode >= 200 && responseCode < 400;
            
            if (!isValid) {
                log.warn("资源链接不可用: {} (状态码: {})", resource.getPanUrl(), responseCode);
            }
            
            // 更新资源的验证状态
            resource.setIsValid(isValid);
            resourceRepository.save(resource);
            
            return isValid;
            
        } catch (Exception e) {
            log.warn("验证资源链接失败: {} - {}", resource.getPanUrl(), e.getMessage());
            
            // 验证失败，标记为不可用
            resource.setIsValid(false);
            resourceRepository.save(resource);
            
            return false;
        }
    }

    /**
     * 批量验证资源可用性
     */
    public int validateResources(List<Resource> resources) {
        int validCount = 0;
        
        for (Resource resource : resources) {
            if (validateResource(resource)) {
                validCount++;
            }
        }
        
        log.info("资源验证完成: {}/{} 个链接可用", validCount, resources.size());
        return validCount;
    }

    /**
     * 验证所有未验证的资源
     */
    public int validateAllUnverified() {
        List<Resource> unverifiedResources = resourceRepository.findByIsValid(true);
        log.info("开始验证{}个资源", unverifiedResources.size());
        return validateResources(unverifiedResources);
    }

    /**
     * 清理无效资源
     */
    public int cleanupInvalidResources() {
        List<Resource> invalidResources = resourceRepository.findByIsValid(false);
        int count = invalidResources.size();
        
        if (count > 0) {
            log.info("清理{}个无效资源", count);
            resourceRepository.deleteAll(invalidResources);
        }
        
        return count;
    }
}
