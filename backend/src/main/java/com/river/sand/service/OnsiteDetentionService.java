package com.river.sand.service;

import com.river.sand.common.BusinessException;
import com.river.sand.entity.*;
import com.river.sand.enums.*;
import com.river.sand.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OnsiteDetentionService {

    private final OnsiteDetentionRepository detentionRepository;
    private final MiningPermitRepository permitRepository;
    private final MiningPermitService permitService;
    private final MiningDeclarationRepository declarationRepository;
    private final AuditService auditService;

    private final AtomicLong detentionCounter = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public OnsiteDetentionService(OnsiteDetentionRepository detentionRepository,
                                  MiningPermitRepository permitRepository,
                                  MiningPermitService permitService,
                                  MiningDeclarationRepository declarationRepository,
                                  AuditService auditService) {
        this.detentionRepository = detentionRepository;
        this.permitRepository = permitRepository;
        this.permitService = permitService;
        this.declarationRepository = declarationRepository;
        this.auditService = auditService;
    }

    public List<OnsiteDetention> getDetectionsByPermit(Long permitId) {
        return detentionRepository.findByPermitIdOrderByDetentionTimeDesc(permitId);
    }

    public List<OnsiteDetention> getDetectionsByDeclaration(Long declarationId) {
        return detentionRepository.findByDeclarationId(declarationId);
    }

    public List<OnsiteDetention> getDetectionsByOfficer(Long officerId) {
        return detentionRepository.findByOfficerId(officerId);
    }

    public Optional<OnsiteDetention> getDetentionById(Long id) {
        return detentionRepository.findById(id);
    }

    public Optional<OnsiteDetention> getDetentionByNo(String detentionNo) {
        return detentionRepository.findByDetentionNo(detentionNo);
    }

    public List<OnsiteDetention> getDetectionsByStatus(DetentionStatus status) {
        return detentionRepository.findByStatus(status);
    }

    @Transactional
    public OnsiteDetention createDetention(OnsiteDetention detention, Long officerId, String officerName) {
        MiningPermit permit = permitRepository.findById(detention.getPermitId())
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        if (permit.getStatus() != PermitStatus.ACTIVE) {
            throw new BusinessException("许可证状态不允许暂扣操作");
        }

        String detentionNo = "ZK" + LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", detentionCounter.incrementAndGet());

        detention.setDetentionNo(detentionNo);
        detention.setPermitNo(permit.getPermitNo());
        detention.setOfficerId(officerId);
        detention.setOfficerName(officerName);
        detention.setStatus(DetentionStatus.DETAINED);
        detention.setDetentionTime(LocalDateTime.now());

        if (detention.getDeclarationId() != null) {
            declarationRepository.findById(detention.getDeclarationId())
                    .ifPresent(decl -> {
                        detention.setDeclarationNo(decl.getDeclarationNo());
                        decl.setStatus(DeclarationStatus.DETAINED);
                        declarationRepository.save(decl);
                    });
        }

        permitService.freezeVolume(detention.getPermitId(), detention.getDetainedVolume());

        OnsiteDetention saved = detentionRepository.save(detention);

        auditService.log("DETENTION", saved.getId(), saved.getDetentionNo(),
                "现场暂扣", officerName, "ENFORCER",
                "现场暂扣，暂扣方量：" + detention.getDetainedVolume() + "方，原因：" + detention.getDetentionReason(),
                officerId);

        auditService.log("PERMIT", permit.getId(), permit.getPermitNo(),
                "现场暂扣", officerName, "ENFORCER",
                "现场暂扣冻结方量：" + detention.getDetainedVolume() + "方，暂扣单号：" + detentionNo,
                officerId);

        return saved;
    }

    @Transactional
    public OnsiteDetention startRectification(Long detentionId, String requirement,
                                               Long officerId, String officerName) {
        OnsiteDetention detention = detentionRepository.findById(detentionId)
                .orElseThrow(() -> new BusinessException("暂扣记录不存在"));

        if (detention.getStatus() != DetentionStatus.DETAINED) {
            throw new BusinessException("当前状态不允许启动整改");
        }

        detention.setStatus(DetentionStatus.RECTIFYING);
        OnsiteDetention saved = detentionRepository.save(detention);

        auditService.log("DETENTION", saved.getId(), saved.getDetentionNo(),
                "启动整改", officerName, "ENFORCER",
                "启动整改，整改要求：" + requirement, officerId);

        return saved;
    }

    @Transactional
    public OnsiteDetention releaseDetention(Long detentionId, String reason,
                                             Long officerId, String officerName) {
        OnsiteDetention detention = detentionRepository.findById(detentionId)
                .orElseThrow(() -> new BusinessException("暂扣记录不存在"));

        if (detention.getStatus() != DetentionStatus.DETAINED &&
                detention.getStatus() != DetentionStatus.REVIEWED) {
            throw new BusinessException("当前状态不允许解除暂扣");
        }

        detention.setStatus(DetentionStatus.RELEASED);
        OnsiteDetention saved = detentionRepository.save(detention);

        permitService.unfreezeVolume(detention.getPermitId(), detention.getDetainedVolume());

        if (detention.getDeclarationId() != null) {
            declarationRepository.findById(detention.getDeclarationId())
                    .ifPresent(decl -> {
                        decl.setStatus(DeclarationStatus.VERIFIED);
                        declarationRepository.save(decl);
                    });
            permitService.frozenToUsed(detention.getPermitId(), detention.getDetainedVolume());
        }

        auditService.log("DETENTION", saved.getId(), saved.getDetentionNo(),
                "解除暂扣", officerName, "ENFORCER",
                "解除暂扣，解除原因：" + reason, officerId);

        auditService.log("PERMIT", detention.getPermitId(), detention.getPermitNo(),
                "解除暂扣", officerName, "ENFORCER",
                "解除暂扣，解冻方量：" + detention.getDetainedVolume() + "方", officerId);

        return saved;
    }

    @Transactional
    public OnsiteDetention confirmViolation(Long detentionId, String reason,
                                             Long officerId, String officerName) {
        OnsiteDetention detention = detentionRepository.findById(detentionId)
                .orElseThrow(() -> new BusinessException("暂扣记录不存在"));

        if (detention.getStatus() != DetentionStatus.REVIEWED) {
            throw new BusinessException("当前状态不允许确认违规");
        }

        detention.setStatus(DetentionStatus.CONFIRMED);
        OnsiteDetention saved = detentionRepository.save(detention);

        permitService.frozenToPenalty(detention.getPermitId(), detention.getDetainedVolume());

        auditService.log("DETENTION", saved.getId(), saved.getDetentionNo(),
                "确认违规", officerName, "ENFORCER",
                "确认违规，确认原因：" + reason, officerId);

        auditService.log("PERMIT", detention.getPermitId(), detention.getPermitNo(),
                "确认违规", officerName, "ENFORCER",
                "确认违规，方量计入处罚：" + detention.getDetainedVolume() + "方", officerId);

        return saved;
    }
}
