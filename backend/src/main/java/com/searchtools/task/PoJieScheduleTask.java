package com.searchtools.task;

import com.searchtools.crawler.PoJieCrawler;
import com.searchtools.model.PoJieResource;
import com.searchtools.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 52pojie定时爬取任务
 * 每10分钟执行一次，爬取吾爱破解论坛的资源并发送邮件通知
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PoJieScheduleTask {

    private final PoJieCrawler poJieCrawler;
    private final EmailNotificationService emailNotificationService;

    /**
     * 定时爬取任务
     * 每10分钟执行一次 (600000毫秒)
     */
    @Scheduled(fixedRate = 600000, initialDelay = 60000) // 首次延迟1分钟执行
    public void crawlAndNotify() {
        log.info("========== 开始52pojie定时爬取任务 ==========");
        log.info("执行时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            // 1. 爬取资源
            List<PoJieResource> newResources = poJieCrawler.crawl();
            log.info("爬取完成，获取{}个新资源", newResources.size());

            // 2. 发送邮件通知
            if (!newResources.isEmpty()) {
                boolean sent = emailNotificationService.sendNewResourceNotification(newResources);
                if (sent) {
                    log.info("邮件通知发送成功");
                } else {
                    log.warn("邮件通知发送失败");
                }
            } else {
                log.info("没有新资源，跳过邮件通知");
            }

        } catch (Exception e) {
            log.error("定时爬取任务执行失败", e);
        }

        log.info("========== 52pojie定时爬取任务结束 ==========");
    }

    /**
     * 检查并发送未通知的资源
     * 每5分钟检查一次
     */
    @Scheduled(fixedRate = 300000, initialDelay = 120000) // 首次延迟2分钟执行
    public void checkAndNotifyPending() {
        log.info("检查未通知的资源...");

        try {
            List<PoJieResource> unnotifiedResources = emailNotificationService.getUnnotifiedResources(50);

            if (!unnotifiedResources.isEmpty()) {
                log.info("发现{}个未通知的资源", unnotifiedResources.size());
                emailNotificationService.sendNewResourceNotification(unnotifiedResources);
            }
        } catch (Exception e) {
            log.error("检查未通知资源失败", e);
        }
    }
}
