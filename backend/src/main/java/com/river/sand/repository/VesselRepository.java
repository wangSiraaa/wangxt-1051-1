package com.river.sand.repository;

import com.river.sand.entity.Vessel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VesselRepository extends JpaRepository<Vessel, Long> {
    Optional<Vessel> findByVesselNo(String vesselNo);
    List<Vessel> findByOwnerId(Long ownerId);
    List<Vessel> findByEnabled(Boolean enabled);
}
