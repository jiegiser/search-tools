package com.searchtools.repository;

import com.searchtools.model.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 搜索历史数据访问接口
 */
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * 查询热门搜索关键词
     */
    @Query(value = "SELECT keyword, COUNT(*) as cnt FROM search_history GROUP BY keyword ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopKeywords(@org.springframework.data.repository.query.Param("limit") int limit);

    /**
     * 查询最近的搜索历史
     */
    Page<SearchHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据关键词查询
     */
    List<SearchHistory> findByKeyword(String keyword);
}
