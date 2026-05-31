package com.searchtools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 网盘资源实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 资源标题
     */
    @Column(nullable = false)
    private String title;

    /**
     * 资源描述
     */
    @Column(length = 2000)
    private String description;

    /**
     * 网盘链接
     */
    @Column(nullable = false, length = 500)
    private String panUrl;

    /**
     * 网盘类型：BAIDU/ALIYUN/QUARK
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PanType panType;

    /**
     * 提取码
     */
    @Column(length = 20)
    private String extractCode;

    /**
     * 资源类型：VIDEO/DOCUMENT/SOFTWARE/MUSIC/OTHER
     */
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    /**
     * 来源URL
     */
    @Column(length = 500)
    private String sourceUrl;

    /**
     * 来源网站
     */
    @Column(length = 200)
    private String sourceSite;

    /**
     * 文件大小（可选）
     */
    @Column(length = 50)
    private String fileSize;

    /**
     * 链接是否有效
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isValid = true;

    /**
     * 点击次数
     */
    @Column(nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * 网盘类型枚举
     */
    public enum PanType {
        BAIDU("百度网盘"),
        ALIYUN("阿里云盘"),
        QUARK("夸克网盘"),
        XUNLEI("迅雷网盘"),
        TIANYI("天翼云盘"),
        UC("UC网盘"),
        PAN115("115网盘"),
        PAN123("123网盘"),
        WEIYUN("微云"),
        LANZOU("蓝奏云"),
        MEGA("MEGA"),
        OTHER("其他");

        private final String displayName;

        PanType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        VIDEO("视频"),
        DOCUMENT("文档"),
        SOFTWARE("软件"),
        MUSIC("音乐"),
        IMAGE("图片"),
        ARCHIVE("压缩包"),
        OTHER("其他");

        private final String displayName;

        ResourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
