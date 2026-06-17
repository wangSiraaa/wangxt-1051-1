package com.river.sand.repository;

import com.river.sand.entity.PenaltyClue;
import com.river.sand.enums.PenaltyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PenaltyClueRepository extends JpaRepository<PenaltyClue, Long> {
    Optional<PenaltyClue> findByClueNo(String clueNo);
    List<PenaltyClue> findByStatus(PenaltyStatus status);
    List<PenaltyClue> findByPermitId(Long permitId);
    List<PenaltyClue> findByEnterpriseId(Long enterpriseId);
    List<PenaltyClue> findByInspectionId(Long inspectionId);
}
