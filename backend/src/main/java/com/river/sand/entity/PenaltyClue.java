package com.river.sand.entity;

import com.river.sand.enums.PenaltyStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "penalty_clue")
public class PenaltyClue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String clueNo;

    @Column(nullable = false)
    private String clueType;

    @Column(nullable = false)
    private String clueSource;

    private Long permitId;

    private String permitNo;

    private Long inspectionId;

    private Long enterpriseId;

    private String enterpriseName;

    @Column(nullable = false)
    private String description;

    private BigDecimal exceedVolume;

    private BigDecimal penaltyAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyStatus status;

    private String reviewOpinion;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String handleResult;

    private Long handlerId;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = PenaltyStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
