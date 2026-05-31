package com.searchtools.repository;

import com.searchtools.model.CrawlRecord;
import com.searchtools.model.CrawlRecord.CrawlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 爬取记录数据访问接口
 */
@Repository
public interface CrawlRecordRepository extends JpaRepository<CrawlRecord, Long> {

    /**
     * 根据状态查询
     */
    Page<CrawlRecord> findByStatus(CrawlStatus status, Pageable pageable);

    /**
     * 查询运行中的任务
     */
    List<CrawlRecord> findByStatus(CrawlStatus status);

    /**
     * 统计运行中的任务数量
     */
    long countByStatus(CrawlStatus status);

    /**
     * 根据URL查询
     */
    CrawlRecord findByUrl(String url);

    /**
     * 查询最近的爬取记录
     */
    Page<CrawlRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
