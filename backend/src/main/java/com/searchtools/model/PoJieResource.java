package com.searchtools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 52pojie资源实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pojie_resources")
public class PoJieResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 帖子标题
     */
    @Column(nullable = false, length = 500)
    private String threadTitle;

    /**
     * 帖子链接
     */
    @Column(nullable = false, length = 500)
    private String threadUrl;

    /**
     * 帖子ID
     */
    @Column(length = 100)
    private String threadId;

    /**
     * 资源链接（网盘链接）
     */
    @Column(nullable = false, length = 1000)
    private String resourceUrl;

    /**
     * 提取码
     */
    @Column(length = 50)
    private String extractCode;

    /**
     * 网盘类型
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Resource.PanType panType;

    /**
     * 资源描述
     */
    @Column(length = 2000)
    private String description;

    /**
     * 发帖人用户名
     */
    @Column(length = 100)
    private String author;

    /**
     * 是否已采纳（回帖是否被采纳为最佳答案）
     */
    @Builder.Default
    private Boolean isAccepted = false;

    /**
     * 是否已通知
     */
    @Builder.Default
    private Boolean notified = false;

    /**
     * 是否有效
     */
    @Builder.Default
    private Boolean isValid = true;

    /**
     * 爬取时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 通知时间
     */
    private LocalDateTime notifiedAt;

    /**
     * 匹配的关键词
     */
    @Column(length = 500)
    private String matchedKeywords;

    /**
     * 帖子回复楼层
     */
    private Integer replyFloor;

    /**
     * 是否为最佳答案回复
     */
    @Builder.Default
    private Boolean isBestAnswer = false;
}
