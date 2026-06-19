package com.searchtools.crawler;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.searchtools.model.Resource;
import com.searchtools.parser.PageParser;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于Playwright的无头浏览器爬虫
 * 能够处理JavaScript渲染的动态页面
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlaywrightCrawler {

    private final PageParser pageParser;
    private final ResourceRepository resourceRepository;

    @Value("${crawler.playwright.headless:true}")
    private boolean headless;

    @Value("${crawler.playwright.timeout:30000}")
    private int timeout;

    /**
     * 使用无头浏览器搜索百度并提取结果
     */
    public List<Resource> searchBaiduWithBrowser(String keyword, int pages) {
        List<Resource> allResources = new ArrayList<>();
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setArgs(List.of("--disable-blink-features=AutomationControlled"))
            );
            
            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setLocale("zh-CN")
            );
            
            // 防止被检测为自动化工具
            context.addInitScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
            
            Page page = context.newPage();
            page.setDefaultTimeout(timeout);
            
            for (int i = 0; i < pages; i++) {
                try {
                    int offset = i * 10;
                    String url = String.format("https://www.baidu.com/s?wd=%s&pn=%d", 
                        java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8), offset);
                    
                    log.info("Playwright百度搜索: {}, 第{}页", keyword, i + 1);
                    
                    // 导航到搜索结果页面
                    page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    );
                    
                    // 等待搜索结果加载
                    page.waitForSelector("#content_left", new Page.WaitForSelectorOptions()
                        .setTimeout(10000)
                    );
                    
                    // 提取搜索结果链接
                    Object result = page.evaluate("() => {\n" +
                        "    const links = [];\n" +
                        "    const results = document.querySelectorAll('#content_left .result h3 a, #content_left .c-container h3 a');\n" +
                        "    results.forEach(a => {\n" +
                        "        if (a.href && a.href.startsWith('http')) {\n" +
                        "            links.push(a.href);\n" +
                        "        }\n" +
                        "    });\n" +
                        "    return links;\n" +
                        "}");
                    @SuppressWarnings("unchecked")
                    List<String> resultUrls = (List<String>) result;
                    
                    log.info("从百度搜索结果中提取到{}个链接", resultUrls.size());
                    
                    // 爬取每个结果页面
                    for (String resultUrl : resultUrls) {
                        try {
                            Resource resource = crawlPageWithBrowser(context, resultUrl);
                            if (resource != null) {
                                List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                                if (existing.isEmpty()) {
                                    resourceRepository.save(resource);
                                    allResources.add(resource);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("爬取搜索结果页面失败: {}", resultUrl, e.getMessage());
                        }
                    }
                    
                    // 避免请求过快
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    log.error("百度搜索第{}页失败", i + 1, e);
                }
            }
            
            browser.close();
            
        } catch (Exception e) {
            log.error("Playwright百度搜索失败", e);
        }
        
        log.info("Playwright百度搜索完成: {}, 共找到{}个资源", keyword, allResources.size());
        return allResources;
    }
    
    /**
     * 使用无头浏览器爬取单个页面
     */
    private Resource crawlPageWithBrowser(BrowserContext context, String url) {
        try {
            Page page = context.newPage();
            page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
            );
            
            // 等待页面加载
            page.waitForTimeout(2000);
            
            // 获取页面HTML
            String html = page.content();
            
            // 使用PageParser解析页面
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html, url);
            List<Resource> resources = pageParser.parsePage(url, doc);
            
            page.close();
            
            if (!resources.isEmpty()) {
                return resources.get(0);
            }
            
        } catch (Exception e) {
            log.debug("爬取页面失败: {}", url, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 使用无头浏览器搜索必应
     */
    public List<Resource> searchBingWithBrowser(String keyword, int pages) {
        List<Resource> allResources = new ArrayList<>();
        
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(headless)
            );
            
            BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setLocale("zh-CN")
            );
            
            Page page = context.newPage();
            page.setDefaultTimeout(timeout);
            
            for (int i = 0; i < pages; i++) {
                try {
                    int offset = i * 10 + 1;
                    String url = String.format("https://www.bing.com/search?q=%s&first=%d", 
                        java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8), offset);
                    
                    log.info("Playwright必应搜索: {}, 第{}页", keyword, i + 1);
                    
                    page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    );
                    
                    // 提取搜索结果
                    Object bingResult = page.evaluate("() => {\n" +
                        "    const links = [];\n" +
                        "    const results = document.querySelectorAll('#b_results .b_algo h2 a');\n" +
                        "    results.forEach(a => {\n" +
                        "        if (a.href && a.href.startsWith('http')) {\n" +
                        "            links.push(a.href);\n" +
                        "        }\n" +
                        "    });\n" +
                        "    return links;\n" +
                        "}");
                    @SuppressWarnings("unchecked")
                    List<String> resultUrls = (List<String>) bingResult;
                    
                    log.info("从必应搜索结果中提取到{}个链接", resultUrls.size());
                    
                    for (String resultUrl : resultUrls) {
                        try {
                            Resource resource = crawlPageWithBrowser(context, resultUrl);
                            if (resource != null) {
                                List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                                if (existing.isEmpty()) {
                                    resourceRepository.save(resource);
                                    allResources.add(resource);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("爬取搜索结果页面失败: {}", resultUrl, e.getMessage());
                        }
                    }
                    
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    log.error("必应搜索第{}页失败", i + 1, e);
                }
            }
            
            browser.close();
            
        } catch (Exception e) {
            log.error("Playwright必应搜索失败", e);
        }
        
        log.info("Playwright必应搜索完成: {}, 共找到{}个资源", keyword, allResources.size());
        return allResources;
    }
}
