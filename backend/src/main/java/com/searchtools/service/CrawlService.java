package com.searchtools.service;

import com.searchtools.crawler.WebCrawler;
import com.searchtools.model.CrawlRecord;
import com.searchtools.model.CrawlRequest;
import com.searchtools.model.Resource;
import com.searchtools.repository.CrawlRecordRepository;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 爬取服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {

    private final WebCrawler webCrawler;
    private final CrawlRecordRepository crawlRecordRepository;
    private final ResourceRepository resourceRepository;

    /**
     * 爬取网页
     *
     * @param request 爬取请求
     * @return 爬取记录
     */
    public CrawlRecord crawl(CrawlRequest request) {
        log.info("开始爬取: {}", request.getUrl());
        return webCrawler.crawlPage(request.getUrl());
    }

    /**
     * 批量爬取
     *
     * @param urls URL列表
     */
    public void crawlBatch(List<String> urls) {
        log.info("批量爬取: {}个URL", urls.size());
        webCrawler.crawlBatch(urls);
    }

    /**
     * 获取爬取记录
     *
     * @param id 记录ID
     * @return 爬取记录
     */
    public CrawlRecord getCrawlRecord(Long id) {
        return crawlRecordRepository.findById(id).orElse(null);
    }

    /**
     * 获取爬取历史
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 爬取记录列表
     */
    public Page<CrawlRecord> getCrawlHistory(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return crawlRecordRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 获取运行中的任务
     *
     * @return 运行中的任务列表
     */
    public List<CrawlRecord> getRunningTasks() {
        return crawlRecordRepository.findByStatus(CrawlRecord.CrawlStatus.RUNNING);
    }

    /**
     * 获取任务状态统计
     *
     * @return 任务状态统计
     */
    public TaskStats getTaskStats() {
        long total = crawlRecordRepository.count();
        long running = crawlRecordRepository.countByStatus(CrawlRecord.CrawlStatus.RUNNING);
        long completed = crawlRecordRepository.countByStatus(CrawlRecord.CrawlStatus.COMPLETED);
        long failed = crawlRecordRepository.countByStatus(CrawlRecord.CrawlStatus.FAILED);

        return TaskStats.builder()
                .total(total)
                .running(running)
                .completed(completed)
                .failed(failed)
                .build();
    }

    /**
     * 获取所有资源
     *
     * @return 资源列表
     */
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    /**
     * 根据ID获取资源
     *
     * @param id 资源ID
     * @return 资源
     */
    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id).orElse(null);
    }

    /**
     * 任务状态统计
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TaskStats {
        private long total;
        private long running;
        private long completed;
        private long failed;
    }
}
