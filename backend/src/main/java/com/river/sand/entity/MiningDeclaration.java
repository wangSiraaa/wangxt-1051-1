package com.river.sand.entity;

import com.river.sand.enums.DeclarationStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mining_declaration")
public class MiningDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String declarationNo;

    @Column(nullable = false)
    private Long permitId;

    @Column(nullable = false)
    private String permitNo;

    @Column(nullable = false)
    private Long holderId;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private Long riverSectionId;

    @Column(nullable = false)
    private String riverSectionName;

    private String miningArea;

    @Column(nullable = false)
    private Long vesselId;

    @Column(nullable = false)
    private String vesselNo;

    @Column(nullable = false)
    private String vesselName;

    @Column(nullable = false)
    private LocalDate declarationDate;

    @Column(nullable = false)
    private BigDecimal declaredVolume;

    private BigDecimal weighedVolume;

    private BigDecimal exceedVolume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeclarationStatus status;

    private String remark;

    private Long submitterId;

    private String submitterName;

    private LocalDateTime submitTime;

    private Long verifierId;

    private String verifierName;

    private LocalDateTime verifyTime;

    private String verifyRemark;

    private Boolean hasPenalty;

    private Long relatedPenaltyId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = DeclarationStatus.DRAFT;
        }
        if (hasPenalty == null) {
            hasPenalty = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
