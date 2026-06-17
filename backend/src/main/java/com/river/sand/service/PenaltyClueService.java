package com.river.sand.service;

import com.river.sand.common.BusinessException;
import com.river.sand.entity.PenaltyClue;
import com.river.sand.entity.User;
import com.river.sand.enums.PenaltyStatus;
import com.river.sand.enums.Role;
import com.river.sand.repository.PenaltyClueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PenaltyClueService {

    private final PenaltyClueRepository penaltyClueRepository;
    private final AuditService auditService;

    public PenaltyClueService(PenaltyClueRepository penaltyClueRepository,
                             AuditService auditService) {
        this.penaltyClueRepository = penaltyClueRepository;
        this.auditService = auditService;
    }

    public List<PenaltyClue> getAllClues() {
        return penaltyClueRepository.findAll();
    }

    public List<PenaltyClue> getCluesByStatus(PenaltyStatus status) {
        return penaltyClueRepository.findByStatus(status);
    }

    public List<PenaltyClue> getCluesByPermit(Long permitId) {
        return penaltyClueRepository.findByPermitId(permitId);
    }

    public List<PenaltyClue> getCluesByEnterprise(Long enterpriseId) {
        return penaltyClueRepository.findByEnterpriseId(enterpriseId);
    }

    public Optional<PenaltyClue> getClueById(Long id) {
        return penaltyClueRepository.findById(id);
    }

    @Transactional
    public PenaltyClue submitForReview(Long clueId, Long operatorId, String operatorName) {
        PenaltyClue clue = penaltyClueRepository.findById(clueId)
                .orElseThrow(() -> new BusinessException("处罚线索不存在"));

        if (clue.getStatus() != PenaltyStatus.PENDING) {
            throw new BusinessException("当前状态不允许提交审核");
        }

        clue.setStatus(PenaltyStatus.REVIEWING);
        PenaltyClue saved = penaltyClueRepository.save(clue);

        auditService.log("PENALTY_CLUE", saved.getId(), saved.getClueNo(),
                "提交审核", operatorName, "ENFORCER",
                "处罚线索提交审核", operatorId);

        return saved;
    }

    @Transactional
    public PenaltyClue review(Long clueId, boolean approved, String opinion,
                            Long reviewerId, String reviewerName) {
        PenaltyClue clue = penaltyClueRepository.findById(clueId)
                .orElseThrow(() -> new BusinessException("处罚线索不存在"));

        if (clue.getStatus() != PenaltyStatus.REVIEWING) {
            throw new BusinessException("当前状态不允许审核");
        }

        clue.setReviewOpinion(opinion);
        clue.setReviewerId(reviewerId);
        clue.setReviewTime(LocalDateTime.now());

        if (approved) {
            clue.setStatus(PenaltyStatus.CONFIRMED);
        } else {
            clue.setStatus(PenaltyStatus.REJECTED);
        }

        PenaltyClue saved = penaltyClueRepository.save(clue);

        auditService.log("PENALTY_CLUE", saved.getId(), saved.getClueNo(),
                approved ? "审核通过" : "审核驳回", reviewerName, "AUDITOR",
                (approved ? "审核通过，确认处罚。" : "审核驳回，") + "意见：" + opinion, reviewerId);

        return saved;
    }

    @Transactional
    public PenaltyClue processClue(Long clueId, String handleResult,
                                Long handlerId, String handlerName) {
        PenaltyClue clue = penaltyClueRepository.findById(clueId)
                .orElseThrow(() -> new BusinessException("处罚线索不存在"));

        if (clue.getStatus() != PenaltyStatus.CONFIRMED) {
            throw new BusinessException("当前状态不允许处理");
        }

        clue.setHandleResult(handleResult);
        clue.setHandlerId(handlerId);
        clue.setHandleTime(LocalDateTime.now());
        clue.setStatus(PenaltyStatus.PROCESSED);

        PenaltyClue saved = penaltyClueRepository.save(clue);

        auditService.log("PENALTY_CLUE", saved.getId(), saved.getClueNo(),
                "处罚处理完成", handlerName, "PENALTY_HANDLER",
                "处罚处理完成，处理结果：" + handleResult, handlerId);

        return saved;
    }
}
