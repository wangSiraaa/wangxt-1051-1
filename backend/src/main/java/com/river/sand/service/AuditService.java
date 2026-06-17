package com.river.sand.service;

import com.river.sand.entity.AuditTimeline;
import com.river.sand.repository.AuditTimelineRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditTimelineRepository auditTimelineRepository;

    public AuditService(AuditTimelineRepository auditTimelineRepository) {
        this.auditTimelineRepository = auditTimelineRepository;
    }

    public void log(String businessType, Long businessId, String businessNo,
                    String operation, String operator, String operatorRole,
                    String detail, Long operatorId) {
        AuditTimeline timeline = new AuditTimeline();
        timeline.setBusinessType(businessType);
        timeline.setBusinessId(businessId);
        timeline.setBusinessNo(businessNo);
        timeline.setOperation(operation);
        timeline.setOperator(operator);
        timeline.setOperatorRole(operatorRole);
        timeline.setDetail(detail);
        timeline.setOperatorId(operatorId);
        timeline.setOperateTime(LocalDateTime.now());
        auditTimelineRepository.save(timeline);
    }

    public List<AuditTimeline> getTimeline(String businessType, Long businessId) {
        return auditTimelineRepository.findByBusinessTypeAndBusinessIdOrderByOperateTimeDesc(
                businessType, businessId);
    }

    public List<AuditTimeline> getLatestTimeline(int limit) {
        return auditTimelineRepository.findTopByOrderByOperateTimeDesc(limit);
    }
}
