package com.searchtools.controller;

import com.searchtools.crawler.DistributedCrawler;
import com.searchtools.crawler.PoJieCrawler;
import com.searchtools.crawler.SearchEngineCrawler;
import com.searchtools.model.CrawlRecord;
import com.searchtools.model.CrawlRequest;
import com.searchtools.model.PoJieResource;
import com.searchtools.model.Resource;
import com.searchtools.repository.PoJieResourceRepository;
import com.searchtools.service.CrawlService;
import com.searchtools.service.EmailNotificationService;
import com.searchtools.service.LinkValidatorService;
import com.searchtools.service.ResourcePreviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬取控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/crawl")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrawlController {

    private final CrawlService crawlService;
    private final SearchEngineCrawler searchEngineCrawler;
    private final LinkValidatorService linkValidatorService;
    private final DistributedCrawler distributedCrawler;
    private final ResourcePreviewService resourcePreviewService;
    private final PoJieCrawler poJieCrawler;
    private final PoJieResourceRepository poJieResourceRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * 爬取网页
     *
     * @param request 爬取请求
     * @return 爬取记录
     */
    @PostMapping
    public ResponseEntity<CrawlRecord> crawl(@Valid @RequestBody CrawlRequest request) {
        CrawlRecord record = crawlService.crawl(request);
        return ResponseEntity.ok(record);
    }

    /**
     * 批量爬取
     *
     * @param urls URL列表
     * @return 操作结果
     */
    @PostMapping("/batch")
    public ResponseEntity<String> crawlBatch(@RequestBody List<String> urls) {
        crawlService.crawlBatch(urls);
        return ResponseEntity.ok("批量爬取任务已提交");
    }

    /**
     * 获取爬取记录
     *
     * @param id 记录ID
     * @return 爬取记录
     */
    @GetMapping("/records/{id}")
    public ResponseEntity<CrawlRecord> getCrawlRecord(@PathVariable Long id) {
        CrawlRecord record = crawlService.getCrawlRecord(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * 获取爬取历史
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 爬取记录列表
     */
    @GetMapping("/history")
    public ResponseEntity<Page<CrawlRecord>> getCrawlHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<CrawlRecord> history = crawlService.getCrawlHistory(page, pageSize);
        return ResponseEntity.ok(history);
    }

    /**
     * 获取运行中的任务
     *
     * @return 运行中的任务列表
     */
    @GetMapping("/running")
    public ResponseEntity<List<CrawlRecord>> getRunningTasks() {
        List<CrawlRecord> tasks = crawlService.getRunningTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取任务状态统计
     *
     * @return 任务状态统计
     */
    @GetMapping("/stats")
    public ResponseEntity<CrawlService.TaskStats> getTaskStats() {
        CrawlService.TaskStats stats = crawlService.getTaskStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 通过搜索引擎搜索关键词
     *
     * @param keyword 搜索关键词
     * @param engine  搜索引擎（baidu/bing/google）
     * @param pages   搜索页数
     * @return 找到的资源数量
     */
    @PostMapping("/search-engine")
    public ResponseEntity<Map<String, Object>> searchByEngine(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "baidu") String engine,
            @RequestParam(defaultValue = "1") int pages) {

        List<Resource> resources;
        if ("bing".equalsIgnoreCase(engine)) {
            resources = searchEngineCrawler.searchBing(keyword, pages);
        } else if ("google".equalsIgnoreCase(engine)) {
            resources = searchEngineCrawler.searchGoogle(keyword, pages);
        } else {
            resources = searchEngineCrawler.searchBaidu(keyword, pages);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("keyword", keyword);
        result.put("engine", engine);
        result.put("pages", pages);
        result.put("resourceCount", resources.size());
        result.put("resources", resources);

        return ResponseEntity.ok(result);
    }

    /**
     * 验证网盘链接是否有效
     *
     * @param panUrl  网盘链接
     * @param panType 网盘类型
     * @return 验证结果
     */
    @PostMapping("/validate-link")
    public ResponseEntity<LinkValidatorService.ValidationResult> validateLink(
            @RequestParam String panUrl,
            @RequestParam(defaultValue = "OTHER") Resource.PanType panType) {

        LinkValidatorService.ValidationResult result = linkValidatorService.validateLink(panUrl, panType);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量验证资源链接
     *
     * @return 验证结果
     */
    @PostMapping("/validate-all")
    public ResponseEntity<Map<String, Object>> validateAllLinks() {
        List<Resource> resources = crawlService.getAllResources();
        int validCount = linkValidatorService.validateBatch(resources);

        Map<String, Object> result = new HashMap<>();
        result.put("total", resources.size());
        result.put("valid", validCount);
        result.put("invalid", resources.size() - validCount);

        return ResponseEntity.ok(result);
    }

    /**
     * 分布式爬取 - 提交单个任务
     *
     * @param url 要爬取的URL
     * @return 任务ID
     */
    @PostMapping("/distributed/submit")
    public ResponseEntity<Map<String, Object>> submitDistributedTask(@RequestParam String url) {
        Long taskId = distributedCrawler.submitTask(url);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("url", url);
        result.put("status", "submitted");

        return ResponseEntity.ok(result);
    }

    /**
     * 分布式爬取 - 批量提交任务
     *
     * @param urls URL列表
     * @return 任务ID列表
     */
    @PostMapping("/distributed/batch")
    public ResponseEntity<Map<String, Object>> submitDistributedBatch(@RequestBody List<String> urls) {
        List<Long> taskIds = distributedCrawler.submitBatchTasks(urls);

        Map<String, Object> result = new HashMap<>();
        result.put("taskIds", taskIds);
        result.put("count", taskIds.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取分布式爬虫状态
     *
     * @return 爬虫状态
     */
    @GetMapping("/distributed/status")
    public ResponseEntity<DistributedCrawler.CrawlerStatus> getDistributedStatus() {
        return ResponseEntity.ok(distributedCrawler.getCrawlerStatus());
    }

    /**
     * 获取分布式任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/distributed/task/{taskId}")
    public ResponseEntity<DistributedCrawler.CrawlTaskStatus> getDistributedTaskStatus(@PathVariable Long taskId) {
        DistributedCrawler.CrawlTaskStatus status = distributedCrawler.getTaskStatus(taskId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    /**
     * 资源预览
     *
     * @param id 资源ID
     * @return 预览结果
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<ResourcePreviewService.PreviewResult> previewResource(@PathVariable Long id) {
        com.searchtools.model.Resource resource = crawlService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        ResourcePreviewService.PreviewResult result = resourcePreviewService.preview(resource);
        return ResponseEntity.ok(result);
    }

    /**
     * 验证链接有效性
     *
     * @param url 链接URL
     * @return 是否有效
     */
    @PostMapping("/check-link")
    public ResponseEntity<Map<String, Object>> checkLink(@RequestParam String url) {
        boolean valid = resourcePreviewService.validateLink(url);

        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("valid", valid);

        return ResponseEntity.ok(result);
    }

    // ==================== 52pojie相关接口 ====================

    /**
     * 手动触发52pojie爬虫
     * 
     * @return 爬取结果
     */
    @PostMapping("/pojie/start")
    public ResponseEntity<Map<String, Object>> startPoJieCrawl() {
        log.info("手动触发52pojie爬虫");
        
        List<PoJieResource> resources = poJieCrawler.crawl();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("resourceCount", resources.size());
        result.put("resources", resources);
        result.put("message", "爬取完成，获取" + resources.size() + "个资源");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取52pojie爬取结果
     * 
     * @param page 页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    @GetMapping("/pojie/results")
    public ResponseEntity<Page<PoJieResource>> getPoJieResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        Page<PoJieResource> resources = poJieResourceRepository.findAll(
                org.springframework.data.domain.PageRequest.of(page, pageSize, 
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        
        return ResponseEntity.ok(resources);
    }

    /**
     * 登录52pojie
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    @PostMapping("/pojie/login")
    public ResponseEntity<Map<String, Object>> loginPoJie(
            @RequestParam String username,
            @RequestParam String password) {
        
        log.info("尝试登录52pojie，用户: {}", username);
        
        boolean success = poJieCrawler.login(username, password);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "登录成功" : "登录失败");
        
        return ResponseEntity.ok(result);
    }

    /**
     * 搜索52pojie资源
     * 
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 每页大小
     * @return 匹配的资源
     */
    @GetMapping("/pojie/search")
    public ResponseEntity<Page<PoJieResource>> searchPoJieResources(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        Page<PoJieResource> resources = poJieResourceRepository.searchByKeyword(keyword,
                org.springframework.data.domain.PageRequest.of(page, pageSize));
        
        return ResponseEntity.ok(resources);
    }

    /**
     * 获取52pojie未通知的资源
     * 
     * @return 未通知的资源列表
     */
    @GetMapping("/pojie/unnotified")
    public ResponseEntity<List<PoJieResource>> getUnnotifiedPoJieResources() {
        List<PoJieResource> resources = emailNotificationService.getUnnotifiedResources(100);
        return ResponseEntity.ok(resources);
    }

    /**
     * 手动发送52pojie资源通知邮件
     * 
     * @return 发送结果
     */
    @PostMapping("/pojie/notify")
    public ResponseEntity<Map<String, Object>> sendPoJieNotification() {
        log.info("手动发送52pojie资源通知");
        
        List<PoJieResource> unnotifiedResources = emailNotificationService.getUnnotifiedResources(50);
        
        Map<String, Object> result = new HashMap<>();
        
        if (unnotifiedResources.isEmpty()) {
            result.put("success", true);
            result.put("message", "没有未通知的资源");
            result.put("count", 0);
        } else {
            boolean sent = emailNotificationService.sendNewResourceNotification(unnotifiedResources);
            result.put("success", sent);
            result.put("message", sent ? "通知发送成功" : "通知发送失败");
            result.put("count", unnotifiedResources.size());
        }
        
        return ResponseEntity.ok(result);
    }
}
