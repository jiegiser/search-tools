package com.searchtools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统状态DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatus {

    /**
     * 系统运行状态
     */
    private String status;

    /**
     * 资源总数
     */
    private Long totalResources;

    /**
     * 有效资源数
     */
    private Long validResources;

    /**
     * 今日新增资源数
     */
    private Long todayResources;

    /**
     * 爬取任务总数
     */
    private Long totalCrawlTasks;

    /**
     * 运行中的爬取任务数
     */
    private Long runningTasks;

    /**
     * 搜索总数
     */
    private Long totalSearches;

    /**
     * 索引大小
     */
    private String indexSize;

    /**
     * 系统启动时间
     */
    private String uptime;

    /**
     * 各网盘资源统计
     */
    private PanStats panStats;

    /**
     * 网盘资源统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PanStats {
        private Long baiduCount;
        private Long aliyunCount;
        private Long quarkCount;
        private Long xunleiCount;
        private Long tianyiCount;
        private Long ucCount;
        private Long pan115Count;
        private Long pan123Count;
        private Long weiyunCount;
        private Long lanzouCount;
        private Long megaCount;
        private Long otherCount;
    }
}
