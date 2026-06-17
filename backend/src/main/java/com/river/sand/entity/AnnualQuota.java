package com.river.sand.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "annual_quota")
public class AnnualQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    private Long riverSectionId;

    private String riverSectionName;

    @Column(nullable = false)
    private BigDecimal totalQuota;

    @Column(nullable = false)
    private BigDecimal usedQuota;

    @Column(nullable = false)
    private BigDecimal remainingQuota;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (usedQuota == null) {
            usedQuota = BigDecimal.ZERO;
        }
        if (remainingQuota == null) {
            remainingQuota = totalQuota;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
