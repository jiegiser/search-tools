package com.searchtools.crawler;

import com.searchtools.model.CrawlRecord;
import com.searchtools.model.Resource;
import com.searchtools.parser.PageParser;
import com.searchtools.repository.CrawlRecordRepository;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网页爬虫
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebCrawler {

    private final PageParser pageParser;
    private final ResourceRepository resourceRepository;
    private final CrawlRecordRepository crawlRecordRepository;

    @Value("${crawler.thread-pool-size:10}")
    private int threadPoolSize;

    @Value("${crawler.request-interval:1000}")
    private long requestInterval;

    @Value("${crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    private ExecutorService executorService;

    /**
     * 初始化线程池
     */
    public void init() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newFixedThreadPool(threadPoolSize);
        }
    }

    /**
     * 爬取单个页面
     *
     * @param url 要爬取的URL
     * @return 爬取记录
     */
    public CrawlRecord crawlPage(String url) {
        init();

        CrawlRecord record = CrawlRecord.builder()
                .url(url)
                .status(CrawlRecord.CrawlStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();
        record = crawlRecordRepository.save(record);

        try {
            // 检查是否已经爬取过
            CrawlRecord existingRecord = crawlRecordRepository.findByUrl(url);
            if (existingRecord != null && existingRecord.getStatus() == CrawlRecord.CrawlStatus.COMPLETED) {
                log.info("URL已爬取过，跳过: {}", url);
                record.setStatus(CrawlRecord.CrawlStatus.COMPLETED);
                record.setResourceCount(existingRecord.getResourceCount());
                record.setCompletedAt(LocalDateTime.now());
                return crawlRecordRepository.save(record);
            }

            // 爬取页面
            Document document = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            // 解析页面，提取资源
            List<Resource> resources = pageParser.parsePage(url, document);

            // 保存资源
            for (Resource resource : resources) {
                // 检查是否已存在
                List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                if (existing.isEmpty()) {
                    resourceRepository.save(resource);
                }
            }

            // 更新爬取记录
            record.setStatus(CrawlRecord.CrawlStatus.COMPLETED);
            record.setResourceCount(resources.size());
            record.setCompletedAt(LocalDateTime.now());

            log.info("爬取完成: {}, 找到{}个资源", url, resources.size());

        } catch (IOException e) {
            log.error("爬取失败: {}", url, e);
            record.setStatus(CrawlRecord.CrawlStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            record.setCompletedAt(LocalDateTime.now());
        }

        return crawlRecordRepository.save(record);
    }

    /**
     * 异步爬取页面
     *
     * @param url 要爬取的URL
     * @return CompletableFuture
     */
    @Async
    public CompletableFuture<CrawlRecord> crawlPageAsync(String url) {
        return CompletableFuture.completedFuture(crawlPage(url));
    }

    /**
     * 批量爬取
     *
     * @param urls URL列表
     */
    public void crawlBatch(List<String> urls) {
        init();

        for (String url : urls) {
            try {
                // 请求间隔限制
                Thread.sleep(requestInterval);
                crawlPageAsync(url);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("爬取被中断", e);
            }
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
