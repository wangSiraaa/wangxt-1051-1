package com.river.sand.repository;

import com.river.sand.entity.OnsiteDetention;
import com.river.sand.enums.DetentionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OnsiteDetentionRepository extends JpaRepository<OnsiteDetention, Long> {
    Optional<OnsiteDetention> findByDetentionNo(String detentionNo);
    List<OnsiteDetention> findByPermitId(Long permitId);
    List<OnsiteDetention> findByPermitIdOrderByDetentionTimeDesc(Long permitId);
    List<OnsiteDetention> findByDeclarationId(Long declarationId);
    List<OnsiteDetention> findByStatus(DetentionStatus status);
    List<OnsiteDetention> findByOfficerId(Long officerId);
    List<OnsiteDetention> findByPermitIdAndStatus(Long permitId, DetentionStatus status);
}
