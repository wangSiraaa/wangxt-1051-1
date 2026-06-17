package com.river.sand.repository;

import com.river.sand.entity.AnnualQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualQuotaRepository extends JpaRepository<AnnualQuota, Long> {
    Optional<AnnualQuota> findByYearAndRiverSectionId(Integer year, Long riverSectionId);
    List<AnnualQuota> findByYear(Integer year);
    List<AnnualQuota> findByRiverSectionId(Long riverSectionId);
}
