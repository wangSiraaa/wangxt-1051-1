package com.river.sand.repository;

import com.river.sand.entity.OnsiteInspection;
import com.river.sand.enums.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnsiteInspectionRepository extends JpaRepository<OnsiteInspection, Long> {
    List<OnsiteInspection> findByPermitId(Long permitId);
    List<OnsiteInspection> findByInspectorId(Long inspectorId);
    List<OnsiteInspection> findByPermitIdAndResult(Long permitId, InspectionResult result);
    Optional<OnsiteInspection> findTopByPermitIdOrderByInspectionTimeDesc(Long permitId);
}
