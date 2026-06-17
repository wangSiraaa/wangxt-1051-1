package com.river.sand.entity;

import com.river.sand.enums.InspectionResult;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "onsite_inspection")
public class OnsiteInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long permitId;

    @Column(nullable = false)
    private String permitNo;

    @Column(nullable = false)
    private Long inspectorId;

    @Column(nullable = false)
    private String inspectorName;

    @Column(nullable = false)
    private LocalDateTime inspectionTime;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String location;

    @Column(nullable = false)
    private BigDecimal actualVolume;

    private String photoPlaceholders;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult result;

    private String abnormalDescription;

    private String remark;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (inspectionTime == null) {
            inspectionTime = LocalDateTime.now();
        }
    }
}
