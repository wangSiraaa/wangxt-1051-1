package com.river.sand.repository;

import com.river.sand.entity.MiningApplication;
import com.river.sand.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MiningApplicationRepository extends JpaRepository<MiningApplication, Long> {
    Optional<MiningApplication> findByApplicationNo(String applicationNo);
    List<MiningApplication> findByApplicantId(Long applicantId);
    List<MiningApplication> findByStatus(ApplicationStatus status);
    List<MiningApplication> findByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);
    List<MiningApplication> findByRiverSectionIdAndStatusIn(Long riverSectionId, List<ApplicationStatus> statuses);
}
