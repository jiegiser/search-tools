package com.searchtools.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 爬取请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlRequest {

    /**
     * 要爬取的URL
     */
    @NotBlank(message = "URL不能为空")
    @Pattern(regexp = "^https?://.*", message = "URL格式不正确")
    private String url;

    /**
     * 是否递归爬取
     */
    @Builder.Default
    private Boolean recursive = false;

    /**
     * 爬取深度
     */
    @Builder.Default
    private Integer depth = 1;
}
