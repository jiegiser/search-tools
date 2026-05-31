package com.searchtools.service;

import com.searchtools.model.Resource;
import com.searchtools.model.SystemStatus;
import com.searchtools.repository.CrawlRecordRepository;
import com.searchtools.repository.ResourceRepository;
import com.searchtools.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 系统状态服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {

    private final ResourceRepository resourceRepository;
    private final CrawlRecordRepository crawlRecordRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    private final LocalDateTime startTime = LocalDateTime.now();

    /**
     * 获取系统状态
     *
     * @return 系统状态
     */
    public SystemStatus getSystemStatus() {
        // 资源统计
        long totalResources = resourceRepository.count();
        long validResources = resourceRepository.countByIsValidTrue();
        long todayResources = resourceRepository.countTodayResources(
                LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN));

        // 爬取任务统计
        long totalCrawlTasks = crawlRecordRepository.count();
        long runningTasks = crawlRecordRepository.countByStatus(
                com.searchtools.model.CrawlRecord.CrawlStatus.RUNNING);

        // 搜索统计
        long totalSearches = searchHistoryRepository.count();

        // 网盘类型统计
        List<Object[]> panTypeStats = resourceRepository.countByPanType();
        SystemStatus.PanStats.PanStatsBuilder panStatsBuilder = SystemStatus.PanStats.builder();

        for (Object[] stat : panTypeStats) {
            Resource.PanType panType = (Resource.PanType) stat[0];
            long count = (Long) stat[1];

            switch (panType) {
                case BAIDU:
                    panStatsBuilder.baiduCount(count);
                    break;
                case ALIYUN:
                    panStatsBuilder.aliyunCount(count);
                    break;
                case QUARK:
                    panStatsBuilder.quarkCount(count);
                    break;
                case XUNLEI:
                    panStatsBuilder.xunleiCount(count);
                    break;
                case TIANYI:
                    panStatsBuilder.tianyiCount(count);
                    break;
                case UC:
                    panStatsBuilder.ucCount(count);
                    break;
                case PAN115:
                    panStatsBuilder.pan115Count(count);
                    break;
                case PAN123:
                    panStatsBuilder.pan123Count(count);
                    break;
                case WEIYUN:
                    panStatsBuilder.weiyunCount(count);
                    break;
                case LANZOU:
                    panStatsBuilder.lanzouCount(count);
                    break;
                case MEGA:
                    panStatsBuilder.megaCount(count);
                    break;
                default:
                    panStatsBuilder.otherCount(count);
                    break;
            }
        }

        return SystemStatus.builder()
                .status("running")
                .totalResources(totalResources)
                .validResources(validResources)
                .todayResources(todayResources)
                .totalCrawlTasks(totalCrawlTasks)
                .runningTasks(runningTasks)
                .totalSearches(totalSearches)
                .indexSize("N/A")
                .uptime(formatUptime())
                .panStats(panStatsBuilder.build())
                .build();
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime() {
        LocalDateTime now = LocalDateTime.now();
        long hours = java.time.Duration.between(startTime, now).toHours();
        long minutes = java.time.Duration.between(startTime, now).toMinutesPart();
        return String.format("%d小时%d分钟", hours, minutes);
    }
}
