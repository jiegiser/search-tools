package com.searchtools.repository;

import com.searchtools.model.PoJieResource;
import com.searchtools.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 52pojie资源数据访问接口
 */
@Repository
public interface PoJieResourceRepository extends JpaRepository<PoJieResource, Long> {

    /**
     * 根据帖子链接查询
     */
    List<PoJieResource> findByThreadUrl(String threadUrl);

    /**
     * 根据资源链接查询
     */
    List<PoJieResource> findByResourceUrl(String resourceUrl);

    /**
     * 查询未通知的资源
     */
    Page<PoJieResource> findByNotifiedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 查询有效的资源
     */
    Page<PoJieResource> findByIsValidTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 查询已采纳的资源
     */
    Page<PoJieResource> findByIsAcceptedTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 查询最佳答案资源
     */
    Page<PoJieResource> findByIsBestAnswerTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据网盘类型查询
     */
    Page<PoJieResource> findByPanType(Resource.PanType panType, Pageable pageable);

    /**
     * 根据关键词模糊查询
     */
    @Query("SELECT p FROM PoJieResource p WHERE p.threadTitle LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<PoJieResource> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 查询最近爬取的资源
     */
    @Query("SELECT p FROM PoJieResource p WHERE p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<PoJieResource> findRecentResources(@Param("since") LocalDateTime since);

    /**
     * 检查资源链接是否已存在
     */
    boolean existsByResourceUrl(String resourceUrl);

    /**
     * 统计未通知的资源数量
     */
    long countByNotifiedFalse();

    /**
     * 批量更新通知状态
     */
    @Query("UPDATE PoJieResource p SET p.notified = true, p.notifiedAt = :now WHERE p.id IN :ids")
    int updateNotifiedStatus(@Param("ids") List<Long> ids, @Param("now") LocalDateTime now);

    /**
     * 查询匹配特定关键词的资源
     */
    @Query("SELECT p FROM PoJieResource p WHERE p.matchedKeywords LIKE %:keyword% AND p.notified = false ORDER BY p.createdAt DESC")
    List<PoJieResource> findByMatchedKeywordNotified(@Param("keyword") String keyword);
}
