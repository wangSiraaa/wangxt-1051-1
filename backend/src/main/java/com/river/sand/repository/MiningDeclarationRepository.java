package com.river.sand.repository;

import com.river.sand.entity.MiningDeclaration;
import com.river.sand.enums.DeclarationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MiningDeclarationRepository extends JpaRepository<MiningDeclaration, Long> {
    Optional<MiningDeclaration> findByDeclarationNo(String declarationNo);
    List<MiningDeclaration> findByPermitId(Long permitId);
    List<MiningDeclaration> findByPermitIdOrderByCreateTimeDesc(Long permitId);
    List<MiningDeclaration> findByHolderId(Long holderId);
    List<MiningDeclaration> findByHolderIdOrderByCreateTimeDesc(Long holderId);
    List<MiningDeclaration> findByStatus(DeclarationStatus status);
    List<MiningDeclaration> findByPermitIdAndStatus(Long permitId, DeclarationStatus status);
    List<MiningDeclaration> findByVesselIdAndDeclarationDate(Long vesselId, LocalDate declarationDate);
    List<MiningDeclaration> findByPermitIdAndDeclarationDateBetween(Long permitId, LocalDate startDate, LocalDate endDate);
}
