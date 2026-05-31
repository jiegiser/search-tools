package com.searchtools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 搜索结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 结果总数
     */
    private Long totalCount;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 搜索耗时（毫秒）
     */
    private Long duration;

    /**
     * 资源列表
     */
    private List<ResourceDTO> resources;

    /**
     * 资源DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceDTO {
        private Long id;
        private String title;
        private String description;
        private String panUrl;
        private String panType;
        private String panTypeName;
        private String extractCode;
        private String resourceType;
        private String resourceTypeName;
        private String sourceUrl;
        private String sourceSite;
        private String fileSize;
        private Boolean isValid;
        private Long clickCount;
        private String createdAt;
    }
}
