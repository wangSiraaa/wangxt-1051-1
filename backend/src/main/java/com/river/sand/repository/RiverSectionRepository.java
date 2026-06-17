package com.river.sand.repository;

import com.river.sand.entity.RiverSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiverSectionRepository extends JpaRepository<RiverSection, Long> {
    Optional<RiverSection> findBySectionCode(String sectionCode);
    List<RiverSection> findByEnabled(Boolean enabled);
}
