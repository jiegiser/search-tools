package com.searchtools.crawler;

import com.searchtools.model.CrawlRecord;
import com.searchtools.model.Resource;
import com.searchtools.parser.PageParser;
import com.searchtools.repository.CrawlRecordRepository;
import com.searchtools.repository.ResourceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分布式爬虫服务
 * 支持多线程爬取、任务队列、爬取任务管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedCrawler {

    private final PageParser pageParser;
    private final ResourceRepository resourceRepository;
    private final CrawlRecordRepository crawlRecordRepository;

    @Value("${crawler.thread-pool-size:10}")
    private int threadPoolSize;

    @Value("${crawler.request-interval:1000}")
    private long requestInterval;

    @Value("${crawler.max-retry:3}")
    private int maxRetry;

    @Value("${crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36}")
    private String userAgent;

    private ExecutorService executorService;
    private final BlockingQueue<CrawlTask> taskQueue = new LinkedBlockingQueue<>();
    private final ConcurrentHashMap<Long, CrawlTaskStatus> taskStatusMap = new ConcurrentHashMap<>();
    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    private volatile boolean running = false;

    /**
     * 爬取任务
     */
    public static class CrawlTask {
        public final Long taskId;
        public final String url;
        public final int priority;

        public CrawlTask(Long taskId, String url, int priority) {
            this.taskId = taskId;
            this.url = url;
            this.priority = priority;
        }
    }

    /**
     * 任务状态
     */
    public static class CrawlTaskStatus {
        public final Long taskId;
        public final String url;
        public volatile String status;
        public volatile int resourceCount;
        public volatile String errorMessage;
        public final LocalDateTime createdAt;
        public volatile LocalDateTime completedAt;

        public CrawlTaskStatus(Long taskId, String url) {
            this.taskId = taskId;
            this.url = url;
            this.status = "PENDING";
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * 系统状态
     */
    public static class CrawlerStatus {
        public final int activeWorkers;
        public final int queueSize;
        public final int totalTasks;
        public final int completedTasks;
        public final int failedTasks;
        public final boolean running;

        public CrawlerStatus(int activeWorkers, int queueSize, int totalTasks,
                           int completedTasks, int failedTasks, boolean running) {
            this.activeWorkers = activeWorkers;
            this.queueSize = queueSize;
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.failedTasks = failedTasks;
            this.running = running;
        }
    }

    @PostConstruct
    public void init() {
        start();
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    /**
     * 启动爬虫系统
     */
    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;
        executorService = Executors.newFixedThreadPool(threadPoolSize);

        // 启动工作线程
        for (int i = 0; i < threadPoolSize; i++) {
            executorService.submit(this::workerLoop);
        }

        log.info("分布式爬虫已启动，线程数: {}", threadPoolSize);
    }

    /**
     * 停止爬虫系统
     */
    public synchronized void stop() {
        running = false;
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("分布式爬虫已停止");
    }

    /**
     * 工作线程循环
     */
    private void workerLoop() {
        while (running) {
            try {
                CrawlTask task = taskQueue.poll(1, TimeUnit.SECONDS);
                if (task != null) {
                    activeWorkers.incrementAndGet();
                    try {
                        processTask(task);
                    } finally {
                        activeWorkers.decrementAndGet();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("工作线程异常", e);
            }
        }
    }

    /**
     * 处理爬取任务
     */
    private void processTask(CrawlTask task) {
        CrawlTaskStatus status = taskStatusMap.get(task.taskId);
        if (status == null) {
            status = new CrawlTaskStatus(task.taskId, task.url);
            taskStatusMap.put(task.taskId, status);
        }

        status.status = "RUNNING";

        // 创建爬取记录
        CrawlRecord record = CrawlRecord.builder()
                .url(task.url)
                .status(CrawlRecord.CrawlStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();
        record = crawlRecordRepository.save(record);

        int retryCount = 0;
        boolean success = false;

        while (retryCount < maxRetry && !success) {
            try {
                // 请求间隔
                if (retryCount > 0) {
                    Thread.sleep(requestInterval * retryCount);
                }

                // 爬取页面
                Document document = Jsoup.connect(task.url)
                        .userAgent(userAgent)
                        .timeout(15000)
                        .followRedirects(true)
                        .get();

                // 解析资源
                List<Resource> resources = pageParser.parsePage(task.url, document);

                // 保存资源
                int savedCount = 0;
                for (Resource resource : resources) {
                    List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                    if (existing.isEmpty()) {
                        resourceRepository.save(resource);
                        savedCount++;
                    }
                }

                // 更新状态
                status.status = "COMPLETED";
                status.resourceCount = savedCount;
                status.completedAt = LocalDateTime.now();

                record.setStatus(CrawlRecord.CrawlStatus.COMPLETED);
                record.setResourceCount(savedCount);
                record.setCompletedAt(LocalDateTime.now());
                crawlRecordRepository.save(record);

                log.info("爬取完成: {}, 保存{}个资源", task.url, savedCount);
                success = true;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                status.status = "FAILED";
                status.errorMessage = "任务被中断";
                break;
            } catch (IOException e) {
                retryCount++;
                log.warn("爬取失败(重试{}/{}): {}, 错误: {}", retryCount, maxRetry, task.url, e.getMessage());
                if (retryCount >= maxRetry) {
                    status.status = "FAILED";
                    status.errorMessage = e.getMessage();
                    status.completedAt = LocalDateTime.now();

                    record.setStatus(CrawlRecord.CrawlStatus.FAILED);
                    record.setErrorMessage(e.getMessage());
                    record.setCompletedAt(LocalDateTime.now());
                    crawlRecordRepository.save(record);
                }
            }
        }

        // 请求间隔
        try {
            Thread.sleep(requestInterval);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 提交爬取任务
     *
     * @param url 要爬取的URL
     * @return 任务ID
     */
    public Long submitTask(String url) {
        CrawlRecord record = CrawlRecord.builder()
                .url(url)
                .status(CrawlRecord.CrawlStatus.PENDING)
                .build();
        record = crawlRecordRepository.save(record);

        CrawlTask task = new CrawlTask(record.getId(), url, 0);
        CrawlTaskStatus status = new CrawlTaskStatus(record.getId(), url);
        taskStatusMap.put(record.getId(), status);

        taskQueue.offer(task);
        log.info("提交爬取任务: {}, ID: {}", url, record.getId());

        return record.getId();
    }

    /**
     * 批量提交爬取任务
     *
     * @param urls URL列表
     * @return 任务ID列表
     */
    public List<Long> submitBatchTasks(List<String> urls) {
        List<Long> taskIds = new ArrayList<>();
        for (String url : urls) {
            taskIds.add(submitTask(url));
        }
        return taskIds;
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    public CrawlTaskStatus getTaskStatus(Long taskId) {
        return taskStatusMap.get(taskId);
    }

    /**
     * 获取爬虫状态
     *
     * @return 爬虫状态
     */
    public CrawlerStatus getCrawlerStatus() {
        int totalTasks = taskStatusMap.size();
        int completedTasks = 0;
        int failedTasks = 0;

        for (CrawlTaskStatus status : taskStatusMap.values()) {
            if ("COMPLETED".equals(status.status)) {
                completedTasks++;
            } else if ("FAILED".equals(status.status)) {
                failedTasks++;
            }
        }

        return new CrawlerStatus(
                activeWorkers.get(),
                taskQueue.size(),
                totalTasks,
                completedTasks,
                failedTasks,
                running
        );
    }

    /**
     * 清理已完成的任务状态
     */
    public void cleanupCompletedTasks() {
        taskStatusMap.entrySet().removeIf(entry -> {
            String status = entry.getValue().status;
            return "COMPLETED".equals(status) || "FAILED".equals(status);
        });
    }
}
