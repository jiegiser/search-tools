package com.searchtools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 搜索历史实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 搜索关键词
     */
    @Column(nullable = false, length = 200)
    private String keyword;

    /**
     * 搜索结果数量
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer resultCount = 0;

    /**
     * 搜索耗时（毫秒）
     */
    private Long duration;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
