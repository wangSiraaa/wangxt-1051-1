package com.river.sand.entity;

import com.river.sand.enums.DetentionStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "onsite_detention")
public class OnsiteDetention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String detentionNo;

    @Column(nullable = false)
    private Long permitId;

    @Column(nullable = false)
    private String permitNo;

    private Long declarationId;

    private String declarationNo;

    @Column(nullable = false)
    private Long vesselId;

    @Column(nullable = false)
    private String vesselName;

    @Column(nullable = false)
    private BigDecimal detainedVolume;

    @Column(nullable = false)
    private String detentionReason;

    private String location;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetentionStatus status;

    @Column(nullable = false)
    private Long officerId;

    @Column(nullable = false)
    private String officerName;

    private LocalDateTime detentionTime;

    private String photoPlaceholders;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = DetentionStatus.DETAINED;
        }
        if (detentionTime == null) {
            detentionTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
