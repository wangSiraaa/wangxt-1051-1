package com.river.sand.repository;

import com.river.sand.entity.ForbiddenPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ForbiddenPeriodRepository extends JpaRepository<ForbiddenPeriod, Long> {
    List<ForbiddenPeriod> findByEnabled(Boolean enabled);
    List<ForbiddenPeriod> findByYearAndEnabled(Integer year, Boolean enabled);
}
