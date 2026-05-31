package com.searchtools.repository;

import com.searchtools.model.Resource;
import com.searchtools.model.Resource.PanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源数据访问接口
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    /**
     * 根据标题模糊查询
     */
    Page<Resource> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 根据网盘类型查询
     */
    Page<Resource> findByPanType(PanType panType, Pageable pageable);

    /**
     * 根据标题和网盘类型查询
     */
    Page<Resource> findByTitleContainingIgnoreCaseAndPanType(String title, PanType panType, Pageable pageable);

    /**
     * 查询有效资源
     */
    Page<Resource> findByIsValidTrue(Pageable pageable);

    /**
     * 统计各网盘类型资源数量
     */
    @Query("SELECT r.panType, COUNT(r) FROM Resource r GROUP BY r.panType")
    List<Object[]> countByPanType();

    /**
     * 统计有效资源数量
     */
    long countByIsValidTrue();

    /**
     * 统计今日新增资源数量
     */
    @Query("SELECT COUNT(r) FROM Resource r WHERE r.createdAt >= :startOfDay")
    long countTodayResources(@Param("startOfDay") LocalDateTime startOfDay);

    /**
     * 根据来源URL查询
     */
    List<Resource> findBySourceUrl(String sourceUrl);

    /**
     * 根据网盘链接查询
     */
    List<Resource> findByPanUrl(String panUrl);

    /**
     * 查询热门资源（按点击量排序）
     */
    Page<Resource> findByIsValidTrueOrderByClickCountDesc(Pageable pageable);

    /**
     * 查询最新资源
     */
    Page<Resource> findByIsValidTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据资源类型查询
     */
    Page<Resource> findByResourceType(Resource.ResourceType resourceType, Pageable pageable);

    /**
     * 根据标题或描述模糊查询（用于搜索后备）
     */
    @Query("SELECT r FROM Resource r WHERE r.title LIKE %:keyword% OR r.description LIKE %:keyword%")
    Page<Resource> findByTitleContainingOrDescriptionContaining(@Param("keyword") String keyword, Pageable pageable);
}
