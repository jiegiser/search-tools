package com.searchtools.crawler;

import com.searchtools.model.PoJieResource;
import com.searchtools.model.Resource;
import com.searchtools.repository.PoJieResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 52pojie.cn专用爬虫
 * 爬取吾爱破解论坛的求助帖，提取网盘资源链接
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PoJieCrawler {

    private final PoJieResourceRepository poJieResourceRepository;

    @Value("${pojie.username:}")
    private String username;

    @Value("${pojie.password:}")
    private String password;

    @Value("${pojie.keywords:Java,C++,前端,架构师,书,电子书,PDF,教程}")
    private String keywords;

    @Value("${pojie.forum-ids:16,65,66,67}")
    private String forumIds;

    @Value("${pojie.pages:3}")
    private int pages;

    @Value("${crawler.request-interval:2000}")
    private long requestInterval;

    private String cookies;
    private boolean loggedIn = false;

    // 网盘链接正则
    private static final Pattern BAIDU_PAN_PATTERN = Pattern.compile(
            "https?://pan\\.baidu\\.com/s/[a-zA-Z0-9_-]+(?:\\?pwd=[a-zA-Z0-9]+)?");
    private static final Pattern LANZOU_PATTERN = Pattern.compile(
            "https?://[a-z0-9]+\\.lanzou[a-z]*\\.com/[a-zA-Z0-9_-]+");
    private static final Pattern ALIYUN_PATTERN = Pattern.compile(
            "https?://(?:www\\.)?ali(?:yun)?drive\\.com/s/[a-zA-Z0-9_-]+");
    private static final Pattern QUARK_PATTERN = Pattern.compile(
            "https?://pan\\.quark\\.cn/s/[a-zA-Z0-9_-]+");
    private static final Pattern XUNLEI_PATTERN = Pattern.compile(
            "https?://pan\\.xunlei\\.com/s/[a-zA-Z0-9_-]+");
    private static final Pattern TIANYI_PATTERN = Pattern.compile(
            "https?://cloud\\.189\\.cn/t/[a-zA-Z0-9_-]+");

    // 提取码正则
    private static final Pattern EXTRACT_CODE_PATTERN = Pattern.compile(
            "(?:提取码|密码|提取密码|访问码|key)[：:\\s]*([a-zA-Z0-9]{4,8})");

    // 52pojie域名
    private static final String BASE_URL = "https://www.52pojie.cn";

    /**
     * 登录52pojie（使用配置的用户名密码）
     *
     * @return 是否登录成功
     */
    public boolean login() {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            log.warn("52pojie用户名或密码未配置");
            return false;
        }
        return doLogin(username, password);
    }

    /**
     * 登录52pojie（使用指定的用户名密码）
     *
     * @param loginUsername 用户名
     * @param loginPassword 密码
     * @return 是否登录成功
     */
    public boolean login(String loginUsername, String loginPassword) {
        if (loginUsername == null || loginUsername.isEmpty() || loginPassword == null || loginPassword.isEmpty()) {
            log.warn("52pojie用户名或密码为空");
            return false;
        }
        return doLogin(loginUsername, loginPassword);
    }

    /**
     * 执行登录操作
     */
    private boolean doLogin(String loginUsername, String loginPassword) {
        try {
            log.info("正在登录52pojie: {}", loginUsername);

            // 获取登录页面
            Connection.Response loginPage = Jsoup.connect(BASE_URL + "/member.php?mod=logging&action=login")
                    .userAgent(getRandomUserAgent())
                    .timeout(15000)
                    .execute();

            // 解析登录表单
            Document loginDoc = loginPage.parse();
            String formHash = loginDoc.select("input[name=formhash]").val();

            if (formHash == null || formHash.isEmpty()) {
                log.error("无法获取formhash");
                return false;
            }

            // 提交登录表单
            Connection.Response loginResponse = Jsoup.connect(BASE_URL + "/member.php?mod=logging&action=login&loginsubmit=yes")
                    .userAgent(getRandomUserAgent())
                    .timeout(15000)
                    .data("formhash", formHash)
                    .data("loginfield", "username")
                    .data("username", loginUsername)
                    .data("password", loginPassword)
                    .data("questionid", "0")
                    .data("answer", "")
                    .data("cookietime", "2592000")
                    .cookies(loginPage.cookies())
                    .method(Connection.Method.POST)
                    .execute();

            // 检查登录结果
            if (loginResponse.url().toString().contains("succeed") ||
                    loginResponse.body().contains("欢迎") ||
                    loginResponse.body().contains(loginUsername)) {
                cookies = loginResponse.cookies().toString();
                loggedIn = true;
                log.info("52pojie登录成功");
                return true;
            } else {
                log.error("52pojie登录失败");
                return false;
            }
        } catch (IOException e) {
            log.error("52pojie登录异常", e);
            return false;
        }
    }

    /**
     * 爬取52pojie论坛
     *
     * @return 爬取到的资源列表
     */
    public List<PoJieResource> crawl() {
        List<PoJieResource> allResources = new ArrayList<>();
        List<String> keywordList = Arrays.asList(keywords.split(","));

        log.info("开始爬取52pojie，关键词: {}", keywordList);

        String[] forumIdArray = forumIds.split(",");
        for (String forumId : forumIdArray) {
            try {
                List<PoJieResource> forumResources = crawlForum(forumId.trim(), keywordList);
                allResources.addAll(forumResources);
            } catch (Exception e) {
                log.error("爬取板块{}失败", forumId, e);
            }
        }

        log.info("52pojie爬取完成，共获取{}个资源", allResources.size());
        return allResources;
    }

    /**
     * 爬取指定板块
     */
    private List<PoJieResource> crawlForum(String forumId, List<String> keywords) throws IOException, InterruptedException {
        List<PoJieResource> resources = new ArrayList<>();

        for (int page = 1; page <= pages; page++) {
            String url = BASE_URL + "/forum-" + forumId + "-" + page + ".html";
            log.info("爬取板块{}, 第{}页: {}", forumId, page, url);

            Thread.sleep(requestInterval + ThreadLocalRandom.current().nextInt(1000));

            Connection connection = Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .timeout(15000)
                    .followRedirects(true);

            if (loggedIn && cookies != null) {
                connection.cookies(parseCookies(cookies));
            }

            Document doc = connection.get();

            // 解析帖子列表
            Elements threadItems = doc.select("tbody[id^=normalthread_]");

            for (Element threadItem : threadItems) {
                try {
                    Element titleLink = threadItem.select("a.s.xst").first();
                    if (titleLink == null) continue;

                    String threadTitle = titleLink.text();
                    String threadUrl = BASE_URL + "/" + titleLink.attr("href");
                    String threadId = extractThreadId(titleLink.attr("href"));

                    // 检查标题是否包含关键词
                    boolean matchesKeyword = false;
                    List<String> matchedKeywords = new ArrayList<>();
                    for (String keyword : keywords) {
                        if (threadTitle.toLowerCase().contains(keyword.toLowerCase().trim())) {
                            matchesKeyword = true;
                            matchedKeywords.add(keyword.trim());
                        }
                    }

                    if (!matchesKeyword) continue;

                    log.info("发现匹配帖子: {}", threadTitle);

                    // 爬取帖子内容
                    List<PoJieResource> threadResources = crawlThread(threadUrl, threadTitle, threadId, matchedKeywords);
                    resources.addAll(threadResources);

                } catch (Exception e) {
                    log.warn("解析帖子失败", e);
                }
            }
        }

        return resources;
    }

    /**
     * 爬取帖子内容，提取网盘链接
     */
    private List<PoJieResource> crawlThread(String threadUrl, String threadTitle, String threadId,
                                             List<String> matchedKeywords) {
        List<PoJieResource> resources = new ArrayList<>();

        try {
            Thread.sleep(requestInterval + ThreadLocalRandom.current().nextInt(500));

            Connection connection = Jsoup.connect(threadUrl)
                    .userAgent(getRandomUserAgent())
                    .timeout(15000)
                    .followRedirects(true);

            if (loggedIn && cookies != null) {
                connection.cookies(parseCookies(cookies));
            }

            Document doc = connection.get();

            // 解析帖子回复
            Elements posts = doc.select("div.t_f");

            int floor = 1;
            for (Element post : posts) {
                String postContent = post.html();
                String postText = post.text();

                // 检查是否为最佳答案（被采纳的回复）
                boolean isBestAnswer = post.parent().select("dl.rate, div.pcb").html().contains("最佳答案") ||
                        post.parent().html().contains("采纳");

                // 提取网盘链接
                List<PanLinkInfo> panLinks = extractPanLinks(postContent, postText);

                for (PanLinkInfo linkInfo : panLinks) {
                    // 检查是否已存在
                    if (poJieResourceRepository.existsByResourceUrl(linkInfo.url)) {
                        continue;
                    }

                    PoJieResource resource = PoJieResource.builder()
                            .threadTitle(threadTitle)
                            .threadUrl(threadUrl)
                            .threadId(threadId)
                            .resourceUrl(linkInfo.url)
                            .extractCode(linkInfo.extractCode)
                            .panType(linkInfo.panType)
                            .description(postText.length() > 200 ? postText.substring(0, 200) + "..." : postText)
                            .author(extractAuthor(doc))
                            .isAccepted(isBestAnswer)
                            .isBestAnswer(isBestAnswer)
                            .matchedKeywords(String.join(",", matchedKeywords))
                            .replyFloor(floor)
                            .notified(false)
                            .isValid(true)
                            .build();

                    poJieResourceRepository.save(resource);
                    resources.add(resource);
                    log.info("保存资源: {} - {} ({})", threadTitle, linkInfo.url, linkInfo.panType);
                }

                floor++;
            }

        } catch (Exception e) {
            log.warn("爬取帖子失败: {}", threadUrl, e);
        }

        return resources;
    }

    /**
     * 提取网盘链接信息
     */
    private List<PanLinkInfo> extractPanLinks(String htmlContent, String textContent) {
        List<PanLinkInfo> links = new ArrayList<>();

        // 提取百度网盘链接
        extractLinks(htmlContent, BAIDU_PAN_PATTERN, Resource.PanType.BAIDU, links, textContent);

        // 提取蓝奏云链接
        extractLinks(htmlContent, LANZOU_PATTERN, Resource.PanType.LANZOU, links, textContent);

        // 提取阿里云盘链接
        extractLinks(htmlContent, ALIYUN_PATTERN, Resource.PanType.ALIYUN, links, textContent);

        // 提取夸克网盘链接
        extractLinks(htmlContent, QUARK_PATTERN, Resource.PanType.QUARK, links, textContent);

        // 提取迅雷网盘链接
        extractLinks(htmlContent, XUNLEI_PATTERN, Resource.PanType.XUNLEI, links, textContent);

        // 提取天翼云盘链接
        extractLinks(htmlContent, TIANYI_PATTERN, Resource.PanType.TIANYI, links, textContent);

        return links;
    }

    /**
     * 提取特定类型的链接
     */
    private void extractLinks(String content, Pattern pattern, Resource.PanType panType,
                              List<PanLinkInfo> links, String textContent) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String url = matcher.group();

            // 提取提取码
            String extractCode = extractCode(content, textContent, url);

            links.add(new PanLinkInfo(url, panType, extractCode));
        }
    }

    /**
     * 提取提取码
     */
    private String extractCode(String htmlContent, String textContent, String url) {
        // 尝试从URL中提取
        if (url.contains("?pwd=")) {
            String[] parts = url.split("\\?pwd=");
            if (parts.length > 1) {
                return parts[1].split("&")[0];
            }
        }

        // 从文本中提取
        Matcher matcher = EXTRACT_CODE_PATTERN.matcher(textContent);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 尝试从URL附近的内容中提取
        int urlIndex = htmlContent.indexOf(url);
        if (urlIndex >= 0) {
            String surrounding = htmlContent.substring(
                    Math.max(0, urlIndex - 200),
                    Math.min(htmlContent.length(), urlIndex + url.length() + 200));
            matcher = EXTRACT_CODE_PATTERN.matcher(surrounding);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    /**
     * 提取帖子作者
     */
    private String extractAuthor(Document doc) {
        Element authorElement = doc.select("a[href*=home.php?mod=space&username=]").first();
        return authorElement != null ? authorElement.text() : "未知";
    }

    /**
     * 提取帖子ID
     */
    private String extractThreadId(String href) {
        Pattern pattern = Pattern.compile("thread-(\\d+)-");
        Matcher matcher = pattern.matcher(href);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析cookies字符串
     */
    private Map<String, String> parseCookies(String cookiesStr) {
        Map<String, String> cookiesMap = new HashMap<>();
        if (cookiesStr == null) return cookiesMap;

        // 移除大括号
        String cleaned = cookiesStr.replace("{", "").replace("}", "");
        String[] pairs = cleaned.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.trim().split("=", 2);
            if (keyValue.length == 2) {
                cookiesMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return cookiesMap;
    }

    /**
     * 获取随机User-Agent
     */
    private String getRandomUserAgent() {
        String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0"
        };
        return userAgents[ThreadLocalRandom.current().nextInt(userAgents.length)];
    }

    /**
     * 获取登录状态
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * 设置登录凭证（用于测试或动态配置）
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 网盘链接信息
     */
    private static class PanLinkInfo {
        final String url;
        final Resource.PanType panType;
        final String extractCode;

        PanLinkInfo(String url, Resource.PanType panType, String extractCode) {
            this.url = url;
            this.panType = panType;
            this.extractCode = extractCode;
        }
    }
}
