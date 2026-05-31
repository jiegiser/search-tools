package com.searchtools.service;

import com.searchtools.model.Resource;
import com.searchtools.model.SearchHistory;
import com.searchtools.model.SearchResult;
import com.searchtools.repository.ResourceRepository;
import com.searchtools.repository.SearchHistoryRepository;
import com.searchtools.search.SearchEngine;
import com.searchtools.crawler.SearchEngineCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchEngine searchEngine;
    private final SearchEngineCrawler searchEngineCrawler;
    private final ResourceRepository resourceRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    /**
     * 搜索资源
     *
     * @param keyword  搜索关键词
     * @param page     页码（从0开始）
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    @Transactional
    public SearchResult search(String keyword, int page, int pageSize) {
        log.info("搜索关键词: {}, 页码: {}, 每页: {}", keyword, page, pageSize);
        long startTime = System.currentTimeMillis();

        // 尝试使用Lucene搜索引擎
        SearchResult result = searchEngine.search(keyword, page, pageSize);

        // 如果Lucene搜索没有结果，使用数据库查询作为后备
        if (result.getTotalCount() == 0) {
            log.info("Lucene搜索无结果，使用数据库查询");
            result = searchFromDatabase(keyword, page, pageSize, startTime);
        }

        // 如果本地无结果，同步爬取全网资源并立即返回
        if (result.getTotalCount() == 0) {
            log.info("本地无结果，同步爬取全网资源: {}", keyword);
            int crawled = crawlWebResources(keyword);
            if (crawled > 0) {
                // 爬取到新资源后，重建索引并重新搜索
                searchEngine.buildIndex();
                result = searchEngine.search(keyword, page, pageSize);
                if (result.getTotalCount() == 0) {
                    // Lucene仍然无结果，尝试数据库搜索
                    result = searchFromDatabase(keyword, page, pageSize, startTime);
                }
                log.info("全网爬取后搜索到{}条结果", result.getTotalCount());
            }
        }

        // 保存搜索历史
        SearchHistory history = SearchHistory.builder()
                .keyword(keyword)
                .resultCount(result.getTotalCount().intValue())
                .duration(result.getDuration())
                .build();
        searchHistoryRepository.save(history);

        return result;
    }

    /**
     * 同步爬取全网资源（百度 + 必应 + Google），返回新入库的资源数量
     */
    private int crawlWebResources(String keyword) {
        int totalNew = 0;
        try {
            log.info("开始全网爬取: {}", keyword);
            // 使用百度搜索
            List<Resource> baiduResults = searchEngineCrawler.searchBaidu(keyword, 1);
            log.info("百度爬取完成: {}个新资源", baiduResults.size());
            totalNew += baiduResults.size();

            // 使用必应搜索
            List<Resource> bingResults = searchEngineCrawler.searchBing(keyword, 1);
            log.info("必应爬取完成: {}个新资源", bingResults.size());
            totalNew += bingResults.size();

            // 使用Google搜索
            List<Resource> googleResults = searchEngineCrawler.searchGoogle(keyword, 1);
            log.info("Google爬取完成: {}个新资源", googleResults.size());
            totalNew += googleResults.size();

            log.info("全网爬取完成，共{}个新资源", totalNew);
        } catch (Exception e) {
            log.error("全网爬取失败: {}", keyword, e);
        }
        return totalNew;
    }

    /**
     * 从数据库搜索（后备方案）
     */
    private SearchResult searchFromDatabase(String keyword, int page, int pageSize, long startTime) {
        Pageable pageable = PageRequest.of(page, pageSize);
        
        // 获取所有资源并手动过滤
        List<Resource> allResources = resourceRepository.findAll();
        // Tokenized search: split keyword into multiple tokens
        // Smart tokenization: split by Chinese/Latin boundaries and spaces
        java.util.List<String> tokenList = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean prevIsChinese = false;
        String lk = keyword.toLowerCase();
        for (int ci = 0; ci < lk.length(); ci++) {
            char ch = lk.charAt(ci);
            boolean isChinese = ch > 127;
            boolean isWhitespace = Character.isWhitespace(ch);
            if (isWhitespace) {
                if (sb.length() > 0) { tokenList.add(sb.toString()); sb.setLength(0); }
                prevIsChinese = false;
                continue;
            }
            if (prevIsChinese && !isChinese && sb.length() > 0) {
                tokenList.add(sb.toString());
                sb.setLength(0);
            } else if (!prevIsChinese && isChinese && sb.length() > 0) {
                tokenList.add(sb.toString());
                sb.setLength(0);
            }
            sb.append(ch);
            prevIsChinese = isChinese;
        }
        if (sb.length() > 0) tokenList.add(sb.toString());
        String[] keywordTokens = tokenList.toArray(new String[0]);
        
        List<Resource> filteredResources = allResources.stream()
                .filter(r -> {
                    String title = r.getTitle() != null ? r.getTitle().toLowerCase() : "";
                    String desc = r.getDescription() != null ? r.getDescription().toLowerCase() : "";
                    for (String kw : keywordTokens) {
                        if (!kw.isEmpty() && !title.contains(kw) && !desc.contains(kw)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        // 手动分页
        int start = page * pageSize;
        int end = Math.min(start + pageSize, filteredResources.size());
        List<Resource> pageContent = filteredResources.subList(start, end);
        
        Page<Resource> resources = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredResources.size());

        List<SearchResult.ResourceDTO> dtos = resources.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return SearchResult.builder()
                .keyword(keyword)
                .totalCount(resources.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .duration(System.currentTimeMillis() - startTime)
                .resources(dtos)
                .build();
    }

    /**
     * 获取资源详情
     *
     * @param id 资源ID
     * @return 资源DTO
     */
    public SearchResult.ResourceDTO getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id).orElse(null);
        if (resource == null) {
            return null;
        }

        // 增加点击次数
        resource.setClickCount(resource.getClickCount() + 1);
        resourceRepository.save(resource);

        return convertToDTO(resource);
    }

    /**
     * 获取热门资源
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    public List<SearchResult.ResourceDTO> getPopularResources(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Resource> resources = resourceRepository.findByIsValidTrueOrderByClickCountDesc(pageable);
        return resources.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取最新资源
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    public List<SearchResult.ResourceDTO> getLatestResources(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Resource> resources = resourceRepository.findByIsValidTrueOrderByCreatedAtDesc(pageable);
        return resources.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门搜索关键词
     *
     * @param limit 数量限制
     * @return 关键词列表
     */
    public List<String> getTopKeywords(int limit) {
        List<Object[]> results = searchHistoryRepository.findTopKeywords(limit);
        return results.stream()
                .map(row -> (String) row[0])
                .collect(Collectors.toList());
    }

    /**
     * 根据网盘类型搜索
     *
     * @param panType  网盘类型
     * @param page     页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    public List<SearchResult.ResourceDTO> getResourcesByPanType(Resource.PanType panType, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Resource> resources = resourceRepository.findByPanType(panType, pageable);
        return resources.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有资源（分页）
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    public SearchResult getAllResources(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<Resource> resources = resourceRepository.findAll(pageable);

        List<SearchResult.ResourceDTO> dtos = resources.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return SearchResult.builder()
                .keyword("")
                .totalCount(resources.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .duration(0L)
                .resources(dtos)
                .build();
    }

    /**
     * 转换为DTO
     */
    private SearchResult.ResourceDTO convertToDTO(Resource resource) {
        return SearchResult.ResourceDTO.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .panUrl(resource.getPanUrl())
                .panType(resource.getPanType().name())
                .panTypeName(resource.getPanType().getDisplayName())
                .extractCode(resource.getExtractCode())
                .resourceType(resource.getResourceType() != null ? resource.getResourceType().name() : "OTHER")
                .resourceTypeName(resource.getResourceType() != null ? resource.getResourceType().getDisplayName() : "其他")
                .sourceUrl(resource.getSourceUrl())
                .sourceSite(resource.getSourceSite())
                .fileSize(resource.getFileSize())
                .isValid(resource.getIsValid())
                .clickCount(resource.getClickCount())
                .createdAt(resource.getCreatedAt() != null ? resource.getCreatedAt().toString() : null)
                .build();
    }
}
