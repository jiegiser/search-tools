package com.searchtools.crawler;

import com.searchtools.model.Resource;
import com.searchtools.parser.PageParser;
import com.searchtools.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 搜索引擎爬虫 - 通过百度、Google等搜索引擎搜索关键词并解析结果
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEngineCrawler {

    private final PageParser pageParser;
    private final ResourceRepository resourceRepository;

    @Value("${crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36}")
    private String userAgent;

    @Value("${crawler.request-interval:2000}")
    private long requestInterval;

    /**
     * 通过百度搜索关键词
     *
     * @param keyword 搜索关键词
     * @param pages   搜索页数
     * @return 找到的资源列表
     */
    public List<Resource> searchBaidu(String keyword, int pages) {
        List<Resource> allResources = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        for (int page = 0; page < pages; page++) {
            try {
                // 百度搜索URL
                int offset = page * 10;
                String url = String.format("https://www.baidu.com/s?wd=%s&pn=%d", encodedKeyword, offset);

                log.info("百度搜索: {}, 第{}页", keyword, page + 1);

                // 随机延迟避免被封
                Thread.sleep(requestInterval + ThreadLocalRandom.current().nextInt(1000));

                Document document = Jsoup.connect(url)
                        .userAgent(getRandomUserAgent())
                        .timeout(15000)
                        .followRedirects(true)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .get();

                // 提取搜索结果中的链接
                List<String> resultUrls = extractBaiduResultUrls(document);
                log.info("从百度搜索结果中提取到{}个链接", resultUrls.size());

                // 爬取每个结果页面
                for (String resultUrl : resultUrls) {
                    try {
                        Thread.sleep(1000);
                        Document resultPage = Jsoup.connect(resultUrl)
                                .userAgent(getRandomUserAgent())
                                .timeout(10000)
                                .followRedirects(true)
                                .get();

                        List<Resource> resources = pageParser.parsePage(resultUrl, resultPage);
                        for (Resource resource : resources) {
                            List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                            if (existing.isEmpty()) {
                                resourceRepository.save(resource);
                                allResources.add(resource);
                            }
                        }
                    } catch (IOException e) {
                        log.warn("爬取搜索结果页面失败: {}", resultUrl, e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("百度搜索被中断", e);
            } catch (IOException e) {
                log.error("百度搜索失败: {}", keyword, e);
            }
        }

        log.info("百度搜索完成: {}, 共找到{}个资源", keyword, allResources.size());
        return allResources;
    }

    /**
     * 通过必应搜索关键词
     *
     * @param keyword 搜索关键词
     * @param pages   搜索页数
     * @return 找到的资源列表
     */
    public List<Resource> searchBing(String keyword, int pages) {
        List<Resource> allResources = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        for (int page = 0; page < pages; page++) {
            try {
                // 必应搜索URL
                int offset = page * 10 + 1;
                String url = String.format("https://www.bing.com/search?q=%s&first=%d", encodedKeyword, offset);

                log.info("必应搜索: {}, 第{}页", keyword, page + 1);

                Thread.sleep(requestInterval + ThreadLocalRandom.current().nextInt(1000));

                Document document = Jsoup.connect(url)
                        .userAgent(getRandomUserAgent())
                        .timeout(15000)
                        .followRedirects(true)
                        .get();

                // 提取搜索结果中的链接
                List<String> resultUrls = extractBingResultUrls(document);
                log.info("从必应搜索结果中提取到{}个链接", resultUrls.size());

                // 爬取每个结果页面
                for (String resultUrl : resultUrls) {
                    try {
                        Thread.sleep(1000);
                        Document resultPage = Jsoup.connect(resultUrl)
                                .userAgent(getRandomUserAgent())
                                .timeout(10000)
                                .followRedirects(true)
                                .get();

                        List<Resource> resources = pageParser.parsePage(resultUrl, resultPage);
                        for (Resource resource : resources) {
                            List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                            if (existing.isEmpty()) {
                                resourceRepository.save(resource);
                                allResources.add(resource);
                            }
                        }
                    } catch (IOException e) {
                        log.warn("爬取搜索结果页面失败: {}", resultUrl, e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("必应搜索被中断", e);
            } catch (IOException e) {
                log.error("必应搜索失败: {}", keyword, e);
            }
        }

        log.info("必应搜索完成: {}, 共找到{}个资源", keyword, allResources.size());
        return allResources;
    }

    /**
     * 从百度搜索结果中提取URL
     */
    private List<String> extractBaiduResultUrls(Document document) {
        List<String> urls = new ArrayList<>();
        // 百度搜索结果在 .result 类中
        Elements results = document.select("div.result h3 a");
        for (Element link : results) {
            String href = link.attr("href");
            if (href != null && !href.isEmpty() && href.startsWith("http")) {
                urls.add(href);
            }
        }

        // 备用选择器
        if (urls.isEmpty()) {
            results = document.select("h3.t a");
            for (Element link : results) {
                String href = link.attr("href");
                if (href != null && !href.isEmpty() && href.startsWith("http")) {
                    urls.add(href);
                }
            }
        }

        return urls;
    }

    /**
     * 通过Google搜索关键词
     *
     * @param keyword 搜索关键词
     * @param pages   搜索页数
     * @return 找到的资源列表
     */
    public List<Resource> searchGoogle(String keyword, int pages) {
        List<Resource> allResources = new ArrayList<>();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        for (int page = 0; page < pages; page++) {
            try {
                // Google搜索URL
                int offset = page * 10;
                String url = String.format("https://www.google.com/search?q=%s&start=%d", encodedKeyword, offset);

                log.info("Google搜索: {}, 第{}页", keyword, page + 1);

                Thread.sleep(requestInterval + ThreadLocalRandom.current().nextInt(1000));

                Document document = Jsoup.connect(url)
                        .userAgent(getRandomUserAgent())
                        .timeout(15000)
                        .followRedirects(true)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.9,zh-CN;q=0.8")
                        .get();

                // 提取搜索结果中的链接
                List<String> resultUrls = extractGoogleResultUrls(document);
                log.info("从Google搜索结果中提取到{}个链接", resultUrls.size());

                // 爬取每个结果页面
                for (String resultUrl : resultUrls) {
                    try {
                        Thread.sleep(1000);
                        Document resultPage = Jsoup.connect(resultUrl)
                                .userAgent(getRandomUserAgent())
                                .timeout(10000)
                                .followRedirects(true)
                                .get();

                        List<Resource> resources = pageParser.parsePage(resultUrl, resultPage);
                        for (Resource resource : resources) {
                            List<Resource> existing = resourceRepository.findByPanUrl(resource.getPanUrl());
                            if (existing.isEmpty()) {
                                resourceRepository.save(resource);
                                allResources.add(resource);
                            }
                        }
                    } catch (IOException e) {
                        log.warn("爬取搜索结果页面失败: {}", resultUrl, e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Google搜索被中断", e);
            } catch (IOException e) {
                log.error("Google搜索失败: {}", keyword, e);
            }
        }

        log.info("Google搜索完成: {}, 共找到{}个资源", keyword, allResources.size());
        return allResources;
    }

    /**
     * 从必应搜索结果中提取URL
     */
    private List<String> extractBingResultUrls(Document document) {
        List<String> urls = new ArrayList<>();
        Elements results = document.select("li.b_algo h2 a");
        for (Element link : results) {
            String href = link.attr("href");
            if (href != null && !href.isEmpty() && href.startsWith("http")) {
                urls.add(href);
            }
        }
        return urls;
    }

    /**
     * 从Google搜索结果中提取URL
     */
    private List<String> extractGoogleResultUrls(Document document) {
        List<String> urls = new ArrayList<>();
        // Google搜索结果的主要选择器
        Elements results = document.select("div.yuRUbf a[href], div.g a[href], div.tF2Cxc a[href]");
        for (Element link : results) {
            String href = link.attr("href");
            if (href != null && !href.isEmpty() && href.startsWith("http")
                    && !href.contains("google.com") && !href.contains("youtube.com/results")) {
                urls.add(href);
            }
        }

        // 备用选择器
        if (urls.isEmpty()) {
            results = document.select("a[href]");
            for (Element link : results) {
                String href = link.attr("href");
                if (href != null && href.startsWith("http")
                        && !href.contains("google.com") && !href.contains("youtube.com")
                        && !href.contains("accounts.google") && !href.contains("support.google")) {
                    urls.add(href);
                }
            }
        }

        return urls;
    }

    /**
     * 获取随机User-Agent
     */
    private String getRandomUserAgent() {
        String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15"
        };
        return userAgents[ThreadLocalRandom.current().nextInt(userAgents.length)];
    }
}
