package com.searchtools.search;

import com.searchtools.model.Resource;
import com.searchtools.model.SearchResult;
import com.searchtools.repository.ResourceRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lucene搜索引擎
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEngine {

    private final ResourceRepository resourceRepository;

    @Value("${search.lucene.index-path:./lucene-index}")
    private String indexPath;

    private Directory directory;
    private IndexWriter indexWriter;
    private SearcherManager searcherManager;
    private final StandardAnalyzer analyzer = new StandardAnalyzer();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化搜索引擎
     */
    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(indexPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        directory = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(directory, config);
        searcherManager = new SearcherManager(indexWriter, false, false, null);

        // 构建初始索引
        buildIndex();

        log.info("搜索引擎初始化完成，索引路径: {}", indexPath);
    }

    /**
     * 构建索引
     */
    public void buildIndex() {
        try {
            // 清空索引
            indexWriter.deleteAll();

            // 获取所有有效资源
            List<Resource> resources = resourceRepository.findByIsValidTrue(PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            for (Resource resource : resources) {
                addDocument(resource);
            }

            indexWriter.commit();
            searcherManager.maybeRefresh();

            log.info("索引构建完成，共{}条记录", resources.size());
        } catch (IOException e) {
            log.error("构建索引失败", e);
        }
    }

    /**
     * 添加文档到索引
     */
    public void addDocument(Resource resource) {
        try {
            Document doc = new Document();

            // ID
            doc.add(new StringField("id", resource.getId().toString(), Field.Store.YES));

            // 标题（可分词）
            doc.add(new TextField("title", resource.getTitle() != null ? resource.getTitle() : "", Field.Store.YES));

            // 描述（可分词）
            doc.add(new TextField("description", resource.getDescription() != null ? resource.getDescription() : "", Field.Store.YES));

            // 网盘链接（不分词）
            doc.add(new StringField("panUrl", resource.getPanUrl(), Field.Store.YES));

            // 网盘类型
            doc.add(new StringField("panType", resource.getPanType().name(), Field.Store.YES));

            // 提取码
            doc.add(new StringField("extractCode", resource.getExtractCode() != null ? resource.getExtractCode() : "", Field.Store.YES));

            // 资源类型
            doc.add(new StringField("resourceType", resource.getResourceType() != null ? resource.getResourceType().name() : "OTHER", Field.Store.YES));

            // 来源URL
            doc.add(new StringField("sourceUrl", resource.getSourceUrl() != null ? resource.getSourceUrl() : "", Field.Store.YES));

            // 来源站点
            doc.add(new StringField("sourceSite", resource.getSourceSite() != null ? resource.getSourceSite() : "", Field.Store.YES));

            // 文件大小
            doc.add(new StringField("fileSize", resource.getFileSize() != null ? resource.getFileSize() : "", Field.Store.YES));

            // 点击次数
            doc.add(new LongPoint("clickCount", resource.getClickCount()));
            doc.add(new StoredField("clickCountStored", resource.getClickCount()));

            // 创建时间
            if (resource.getCreatedAt() != null) {
                doc.add(new StringField("createdAt", resource.getCreatedAt().format(DATE_FORMATTER), Field.Store.YES));
            }

            indexWriter.addDocument(doc);
        } catch (IOException e) {
            log.error("添加文档失败: {}", resource.getId(), e);
        }
    }

    /**
     * 搜索资源
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    public SearchResult search(String keyword, int page, int pageSize) {
        long startTime = System.currentTimeMillis();

        try {
            searcherManager.maybeRefresh();
            IndexSearcher searcher = searcherManager.acquire();

            try {
                // 使用WildcardQuery支持中文搜索
                // 转义Lucene特殊字符
                String escapedKeyword = escapeLuceneSpecial(keyword.toLowerCase());
                
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                
                // 在标题和描述中搜索
                Query titleQuery = new WildcardQuery(new Term("title", "*" + escapedKeyword + "*"));
                Query descQuery = new WildcardQuery(new Term("description", "*" + escapedKeyword + "*"));
                
                builder.add(titleQuery, BooleanClause.Occur.SHOULD);
                builder.add(descQuery, BooleanClause.Occur.SHOULD);
                
                Query query = builder.build();

                // 执行搜索
                int start = page * pageSize;
                TopDocs topDocs = searcher.search(query, start + pageSize, Sort.RELEVANCE);

                // 转换结果
                List<SearchResult.ResourceDTO> resourceDTOs = new ArrayList<>();
                ScoreDoc[] hits = topDocs.scoreDocs;

                for (int i = start; i < Math.min(hits.length, start + pageSize); i++) {
                    Document doc = searcher.doc(hits[i].doc);
                    resourceDTOs.add(convertToDTO(doc));
                }

                long duration = System.currentTimeMillis() - startTime;

                return SearchResult.builder()
                        .keyword(keyword)
                        .totalCount(topDocs.totalHits.value)
                        .page(page)
                        .pageSize(pageSize)
                        .duration(duration)
                        .resources(resourceDTOs)
                        .build();

            } finally {
                searcherManager.release(searcher);
            }

        } catch (IOException e) {
            log.error("搜索失败: {}", keyword, e);
            return SearchResult.builder()
                    .keyword(keyword)
                    .totalCount(0L)
                    .page(page)
                    .pageSize(pageSize)
                    .duration(System.currentTimeMillis() - startTime)
                    .resources(Collections.emptyList())
                    .build();
        }
    }

    /**
     * 更新索引
     */
    public void updateIndex(Resource resource) {
        try {
            // 删除旧文档
            Term term = new Term("id", resource.getId().toString());
            indexWriter.deleteDocuments(term);

            // 添加新文档
            addDocument(resource);

            indexWriter.commit();
            searcherManager.maybeRefresh();
        } catch (IOException e) {
            log.error("更新索引失败: {}", resource.getId(), e);
        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex(Long resourceId) {
        try {
            Term term = new Term("id", resourceId.toString());
            indexWriter.deleteDocuments(term);
            indexWriter.commit();
            searcherManager.maybeRefresh();
        } catch (IOException e) {
            log.error("删除索引失败: {}", resourceId, e);
        }
    }

    /**
     * 转换为DTO
     */
    private SearchResult.ResourceDTO convertToDTO(Document doc) {
        return SearchResult.ResourceDTO.builder()
                .id(Long.parseLong(doc.get("id")))
                .title(doc.get("title"))
                .description(doc.get("description"))
                .panUrl(doc.get("panUrl"))
                .panType(doc.get("panType"))
                .panTypeName(getPanTypeName(doc.get("panType")))
                .extractCode(doc.get("extractCode"))
                .resourceType(doc.get("resourceType"))
                .resourceTypeName(getResourceTypeName(doc.get("resourceType")))
                .sourceUrl(doc.get("sourceUrl"))
                .sourceSite(doc.get("sourceSite"))
                .fileSize(doc.get("fileSize"))
                .isValid(true)
                .clickCount(Long.parseLong(doc.get("clickCountStored")))
                .createdAt(doc.get("createdAt"))
                .build();
    }

    /**
     * 获取网盘类型名称
     */
    private String getPanTypeName(String panType) {
        try {
            return Resource.PanType.valueOf(panType).getDisplayName();
        } catch (Exception e) {
            return panType;
        }
    }

    /**
     * 获取资源类型名称
     */
    private String getResourceTypeName(String resourceType) {
        try {
            return Resource.ResourceType.valueOf(resourceType).getDisplayName();
        } catch (Exception e) {
            return resourceType;
        }
    }

    /**
     * 转义Lucene特殊字符
     */
    private String escapeLuceneSpecial(String keyword) {
        // Lucene特殊字符: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
        StringBuilder sb = new StringBuilder();
        for (char c : keyword.toCharArray()) {
            if ("+-&|!(){}[]^\"~*?:\\/".indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 销毁搜索引擎
     */
    @PreDestroy
    public void destroy() {
        try {
            if (searcherManager != null) {
                searcherManager.close();
            }
            if (indexWriter != null) {
                indexWriter.close();
            }
            if (directory != null) {
                directory.close();
            }
            log.info("搜索引擎已关闭");
        } catch (IOException e) {
            log.error("关闭搜索引擎失败", e);
        }
    }
}
