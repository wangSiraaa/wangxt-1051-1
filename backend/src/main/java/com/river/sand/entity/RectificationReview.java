package com.river.sand.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rectification_review")
public class RectificationReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long detentionId;

    private String detentionNo;

    private Long penaltyClueId;

    private String reviewNo;

    @Column(nullable = false)
    private String rectificationRequirement;

    private String rectificationResult;

    private Boolean isRectified;

    private String reviewOpinion;

    @Column(nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private String reviewerName;

    private LocalDateTime reviewTime;

    private String photoPlaceholders;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (reviewTime == null) {
            reviewTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
