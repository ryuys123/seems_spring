package com.test.seems.content.jpa.entity;

import lombok.*;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TB_CONTENT_RECOMMENDATIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentEntity {
    @Id
    @Column(name = "CONTENT_ID")
    private Long contentId;

    @Column(name = "YOUTUBE_ID", nullable = false, unique = true, length = 20)
    private String youtubeId;

    @Column(name = "THEME")
    private String theme;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "DESCRIPTION", length = 2000)
    private String description;

    @Column(name = "THUMBNAIL_URL", length = 500)
    private String thumbnailUrl;

    @Column(name = "DURATION", length = 10)
    private String duration;

    @Column(name = "VIEW_COUNT")
    private Long viewCount;

    @Column(name = "PUBLISHED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishedAt;

    @Column(name = "IS_ACTIVE")
    private Integer isActive;

    @Column(name = "CREATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "UPDATED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
