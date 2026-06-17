package com.river.sand.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_timeline")
public class AuditTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String businessType;

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private String businessNo;

    @Column(nullable = false)
    private String operation;

    private String operator;

    private String operatorRole;

    private String detail;

    private Long operatorId;

    @Column(nullable = false)
    private LocalDateTime operateTime;

    @PrePersist
    protected void onCreate() {
        operateTime = LocalDateTime.now();
    }
}
