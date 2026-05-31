package com.searchtools.controller;

import com.searchtools.model.Resource;
import com.searchtools.model.SearchResult;
import com.searchtools.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 搜索资源
     *
     * @param keyword  搜索关键词
     * @param page     页码（从0开始）
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SearchResult result = searchService.search(keyword.trim(), page, pageSize);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取资源详情
     *
     * @param id 资源ID
     * @return 资源详情
     */
    @GetMapping("/resources/{id}")
    public ResponseEntity<SearchResult.ResourceDTO> getResource(@PathVariable Long id) {
        SearchResult.ResourceDTO resource = searchService.getResourceById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resource);
    }

    /**
     * 获取热门资源
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    @GetMapping("/resources/popular")
    public ResponseEntity<List<SearchResult.ResourceDTO>> getPopularResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<SearchResult.ResourceDTO> resources = searchService.getPopularResources(page, pageSize);
        return ResponseEntity.ok(resources);
    }

    /**
     * 获取最新资源
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    @GetMapping("/resources/latest")
    public ResponseEntity<List<SearchResult.ResourceDTO>> getLatestResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<SearchResult.ResourceDTO> resources = searchService.getLatestResources(page, pageSize);
        return ResponseEntity.ok(resources);
    }

    /**
     * 根据网盘类型获取资源
     *
     * @param panType  网盘类型
     * @param page     页码
     * @param pageSize 每页大小
     * @return 资源列表
     */
    @GetMapping("/resources/pan/{panType}")
    public ResponseEntity<List<SearchResult.ResourceDTO>> getResourcesByPanType(
            @PathVariable Resource.PanType panType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        List<SearchResult.ResourceDTO> resources = searchService.getResourcesByPanType(panType, page, pageSize);
        return ResponseEntity.ok(resources);
    }

    /**
     * 获取热门搜索关键词
     *
     * @param limit 数量限制
     * @return 关键词列表
     */
    @GetMapping("/keywords/hot")
    public ResponseEntity<List<String>> getHotKeywords(
            @RequestParam(defaultValue = "10") int limit) {

        List<String> keywords = searchService.getTopKeywords(limit);
        return ResponseEntity.ok(keywords);
    }

    /**
     * 获取所有资源（分页）
     *
     * @param page     页码
     * @param pageSize 每页大小
     * @return 搜索结果
     */
    @GetMapping("/resources/all")
    public ResponseEntity<SearchResult> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        SearchResult result = searchService.getAllResources(page, pageSize);
        return ResponseEntity.ok(result);
    }
}
