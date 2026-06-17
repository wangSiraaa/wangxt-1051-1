package com.river.sand.entity;

import com.river.sand.enums.PermitStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mining_permit")
public class MiningPermit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String permitNo;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long holderId;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private Long riverSectionId;

    @Column(nullable = false)
    private String riverSectionName;

    private String vesselIds;

    private String vesselNames;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private BigDecimal permittedVolume;

    @Column(nullable = false)
    private BigDecimal usedVolume;

    @Column(nullable = false)
    private BigDecimal remainingVolume;

    @Column(nullable = false)
    private BigDecimal frozenVolume;

    @Column(nullable = false)
    private BigDecimal penaltyVolume;

    @Column(nullable = false)
    private BigDecimal depositAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermitStatus status;

    private String suspendReason;

    private String changeDescription;

    private LocalDateTime issueDate;

    private LocalDateTime suspendTime;

    private LocalDateTime resumeTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        updateTime = LocalDateTime.now();
        if (usedVolume == null) {
            usedVolume = BigDecimal.ZERO;
        }
        if (remainingVolume == null) {
            remainingVolume = permittedVolume;
        }
        if (frozenVolume == null) {
            frozenVolume = BigDecimal.ZERO;
        }
        if (penaltyVolume == null) {
            penaltyVolume = BigDecimal.ZERO;
        }
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
