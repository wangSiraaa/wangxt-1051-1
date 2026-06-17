package com.river.sand.repository;

import com.river.sand.entity.AuditTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditTimelineRepository extends JpaRepository<AuditTimeline, Long> {
    List<AuditTimeline> findByBusinessTypeAndBusinessIdOrderByOperateTimeDesc(String businessType, Long businessId);
    List<AuditTimeline> findTopByOrderByOperateTimeDesc(int limit);
}
