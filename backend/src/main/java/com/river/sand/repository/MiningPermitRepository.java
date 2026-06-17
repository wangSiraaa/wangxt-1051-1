package com.river.sand.repository;

import com.river.sand.entity.MiningPermit;
import com.river.sand.enums.PermitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MiningPermitRepository extends JpaRepository<MiningPermit, Long> {
    Optional<MiningPermit> findByPermitNo(String permitNo);
    Optional<MiningPermit> findByApplicationId(Long applicationId);
    List<MiningPermit> findByHolderId(Long holderId);
    List<MiningPermit> findByStatus(PermitStatus status);
    List<MiningPermit> findByHolderIdAndStatus(Long holderId, PermitStatus status);
    List<MiningPermit> findByRiverSectionIdAndStatusIn(Long riverSectionId, List<PermitStatus> statuses);
    List<MiningPermit> findByStatusAndEndDateBefore(PermitStatus status, LocalDate date);
}
