package com.searchtools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 爬取记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crawl_records")
public class CrawlRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 爬取的URL
     */
    @Column(nullable = false, length = 500)
    private String url;

    /**
     * 状态：PENDING/RUNNING/COMPLETED/FAILED
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CrawlStatus status = CrawlStatus.PENDING;

    /**
     * 找到的资源数量
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer resourceCount = 0;

    /**
     * 错误信息
     */
    @Column(length = 1000)
    private String errorMessage;

    /**
     * 爬取开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 爬取结束时间
     */
    private LocalDateTime completedAt;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 爬取状态枚举
     */
    public enum CrawlStatus {
        PENDING("待处理"),
        RUNNING("运行中"),
        COMPLETED("已完成"),
        FAILED("失败");

        private final String displayName;

        CrawlStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
