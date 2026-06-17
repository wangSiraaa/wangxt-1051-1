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
public class MiningDeclarationService {

    private final MiningDeclarationRepository declarationRepository;
    private final MiningPermitRepository permitRepository;
    private final MiningPermitService permitService;
    private final VesselRepository vesselRepository;
    private final ForbiddenPeriodService forbiddenPeriodService;
    private final PenaltyClueRepository penaltyClueRepository;
    private final AuditService auditService;

    private final AtomicLong declarationCounter = new AtomicLong(0);
    private final AtomicLong clueCounter = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public MiningDeclarationService(MiningDeclarationRepository declarationRepository,
                                    MiningPermitRepository permitRepository,
                                    MiningPermitService permitService,
                                    VesselRepository vesselRepository,
                                    ForbiddenPeriodService forbiddenPeriodService,
                                    PenaltyClueRepository penaltyClueRepository,
                                    AuditService auditService) {
        this.declarationRepository = declarationRepository;
        this.permitRepository = permitRepository;
        this.permitService = permitService;
        this.vesselRepository = vesselRepository;
        this.forbiddenPeriodService = forbiddenPeriodService;
        this.penaltyClueRepository = penaltyClueRepository;
        this.auditService = auditService;
    }

    public List<MiningDeclaration> getDeclarationsByPermit(Long permitId) {
        return declarationRepository.findByPermitIdOrderByCreateTimeDesc(permitId);
    }

    public List<MiningDeclaration> getDeclarationsByHolder(Long holderId) {
        return declarationRepository.findByHolderIdOrderByCreateTimeDesc(holderId);
    }

    public Optional<MiningDeclaration> getDeclarationById(Long id) {
        return declarationRepository.findById(id);
    }

    public Optional<MiningDeclaration> getDeclarationByNo(String declarationNo) {
        return declarationRepository.findByDeclarationNo(declarationNo);
    }

    public List<MiningDeclaration> getDeclarationsByStatus(DeclarationStatus status) {
        return declarationRepository.findByStatus(status);
    }

    @Transactional
    public MiningDeclaration createDeclaration(MiningDeclaration declaration, Long userId, String userName) {
        MiningPermit permit = permitRepository.findById(declaration.getPermitId())
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        if (permit.getStatus() != PermitStatus.ACTIVE) {
            throw new BusinessException("许可证状态不允许申报");
        }

        Vessel vessel = vesselRepository.findById(declaration.getVesselId())
                .orElseThrow(() -> new BusinessException("船只不存在"));

        validateVesselInPermit(permit, vessel.getId());

        String declarationNo = "SB" + LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", declarationCounter.incrementAndGet());

        declaration.setDeclarationNo(declarationNo);
        declaration.setPermitNo(permit.getPermitNo());
        declaration.setHolderId(permit.getHolderId());
        declaration.setHolderName(permit.getHolderName());
        declaration.setRiverSectionId(permit.getRiverSectionId());
        declaration.setRiverSectionName(permit.getRiverSectionName());
        declaration.setVesselNo(vessel.getVesselNo());
        declaration.setVesselName(vessel.getVesselName());
        declaration.setStatus(DeclarationStatus.DRAFT);
        declaration.setSubmitterId(userId);
        declaration.setSubmitterName(userName);

        if (declaration.getDeclarationDate() == null) {
            declaration.setDeclarationDate(LocalDate.now());
        }

        MiningDeclaration saved = declarationRepository.save(declaration);

        auditService.log("DECLARATION", saved.getId(), saved.getDeclarationNo(),
                "创建申报", userName, "ENTERPRISE",
                "创建采砂申报单，申报方量：" + declaration.getDeclaredVolume() + "方", userId);

        return saved;
    }

    @Transactional
    public MiningDeclaration submitDeclaration(Long declarationId, Long userId, String userName) {
        MiningDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new BusinessException("申报单不存在"));

        if (declaration.getStatus() != DeclarationStatus.DRAFT) {
            throw new BusinessException("当前状态不允许提交申报");
        }

        MiningPermit permit = permitRepository.findById(declaration.getPermitId())
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        validateForSubmission(permit, declaration);

        declaration.setStatus(DeclarationStatus.SUBMITTED);
        declaration.setSubmitterId(userId);
        declaration.setSubmitterName(userName);
        declaration.setSubmitTime(LocalDateTime.now());

        MiningDeclaration saved = declarationRepository.save(declaration);

        auditService.log("DECLARATION", saved.getId(), saved.getDeclarationNo(),
                "提交申报", userName, "ENTERPRISE",
                "提交采砂申报，申报方量：" + declaration.getDeclaredVolume() + "方", userId);

        return saved;
    }

    @Transactional
    public MiningDeclaration verifyDeclaration(Long declarationId, BigDecimal weighedVolume,
                                                String verifyRemark, Long verifierId, String verifierName) {
        MiningDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new BusinessException("申报单不存在"));

        if (declaration.getStatus() != DeclarationStatus.SUBMITTED) {
            throw new BusinessException("当前状态不允许核验");
        }

        MiningPermit permit = permitRepository.findById(declaration.getPermitId())
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        declaration.setWeighedVolume(weighedVolume);
        declaration.setVerifierId(verifierId);
        declaration.setVerifierName(verifierName);
        declaration.setVerifyTime(LocalDateTime.now());
        declaration.setVerifyRemark(verifyRemark);

        BigDecimal availableVolume = permitService.getAvailableVolume(declaration.getPermitId());

        if (weighedVolume.compareTo(availableVolume) > 0) {
            BigDecimal exceedVolume = weighedVolume.subtract(availableVolume);
            declaration.setExceedVolume(exceedVolume);
            declaration.setStatus(DeclarationStatus.ABNORMAL);
            declaration.setHasPenalty(true);

            permitService.freezeVolume(declaration.getPermitId(), weighedVolume);

            PenaltyClue clue = createPenaltyClueFromDeclaration(permit, declaration, exceedVolume,
                    verifierId, verifierName);
            declaration.setRelatedPenaltyId(clue.getId());

            auditService.log("DECLARATION", declaration.getId(), declaration.getDeclarationNo(),
                    "核验异常", verifierName, "ENFORCER",
                    "现场核验发现超量，超采量：" + exceedVolume + "方，已生成处罚线索", verifierId);
        } else {
            declaration.setStatus(DeclarationStatus.VERIFIED);
            declaration.setExceedVolume(BigDecimal.ZERO);

            permitService.addUsedVolume(declaration.getPermitId(), weighedVolume);

            auditService.log("DECLARATION", declaration.getId(), declaration.getDeclarationNo(),
                    "核验通过", verifierName, "ENFORCER",
                    "现场核验通过，实际方量：" + weighedVolume + "方", verifierId);
        }

        return declarationRepository.save(declaration);
    }

    @Transactional
    public MiningDeclaration cancelDeclaration(Long declarationId, Long userId, String userName) {
        MiningDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new BusinessException("申报单不存在"));

        if (declaration.getStatus() != DeclarationStatus.DRAFT &&
                declaration.getStatus() != DeclarationStatus.SUBMITTED) {
            throw new BusinessException("当前状态不允许取消");
        }

        declaration.setStatus(DeclarationStatus.CANCELLED);

        MiningDeclaration saved = declarationRepository.save(declaration);

        auditService.log("DECLARATION", saved.getId(), saved.getDeclarationNo(),
                "取消申报", userName, "ENTERPRISE",
                "取消采砂申报", userId);

        return saved;
    }

    private void validateForSubmission(MiningPermit permit, MiningDeclaration declaration) {
        if (permit.getStatus() != PermitStatus.ACTIVE) {
            throw new BusinessException("许可证状态不允许申报");
        }

        if (permit.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessException("许可证已到期");
        }

        if (forbiddenPeriodService.isInForbiddenPeriod(declaration.getDeclarationDate())) {
            ForbiddenPeriod fp = forbiddenPeriodService.getForbiddenPeriodForDate(declaration.getDeclarationDate());
            String desc = fp != null ? fp.getDescription() : "停采期";
            throw new BusinessException("申报日期处于停采期（" + desc + "），不允许申报");
        }

        validateVesselInPermit(permit, declaration.getVesselId());

        BigDecimal availableVolume = permitService.getAvailableVolume(permit.getId());
        if (declaration.getDeclaredVolume().compareTo(availableVolume) > 0) {
            throw new BusinessException("申报方量超过剩余可用额度，剩余可用：" + availableVolume + "方");
        }
    }

    private void validateVesselInPermit(MiningPermit permit, Long vesselId) {
        String vesselIdsStr = permit.getVesselIds();
        if (vesselIdsStr == null || vesselIdsStr.isEmpty()) {
            throw new BusinessException("许可证未配置作业船队");
        }

        String[] vesselIds = vesselIdsStr.split(",");
        boolean found = false;
        for (String idStr : vesselIds) {
            if (idStr.trim().equals(String.valueOf(vesselId))) {
                found = true;
                break;
            }
        }

        if (!found) {
            throw new BusinessException("该船只不在许可船队范围内");
        }
    }

    private PenaltyClue createPenaltyClueFromDeclaration(MiningPermit permit, MiningDeclaration declaration,
                                                          BigDecimal exceedVolume, Long operatorId, String operatorName) {
        PenaltyClue clue = new PenaltyClue();
        String clueNo = "XZ" + LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", clueCounter.incrementAndGet());
        clue.setClueNo(clueNo);
        clue.setClueType("超许可方量");
        clue.setClueSource("申报核验");
        clue.setPermitId(permit.getId());
        clue.setPermitNo(permit.getPermitNo());
        clue.setEnterpriseId(permit.getHolderId());
        clue.setEnterpriseName(permit.getHolderName());
        clue.setDescription("第" + declaration.getDeclarationNo() + "号申报单现场称重核验发现超量开采，" +
                "申报方量：" + declaration.getDeclaredVolume() + "方，" +
                "实际称重：" + declaration.getWeighedVolume() + "方，" +
                "超采量：" + exceedVolume + "方。" +
                "该处罚仅关联本船次，不影响其他合规船次的正常作业。");
        clue.setExceedVolume(exceedVolume);
        clue.setPenaltyAmount(calculatePenalty(exceedVolume));
        clue.setStatus(PenaltyStatus.PENDING);

        PenaltyClue savedClue = penaltyClueRepository.save(clue);

        auditService.log("PENALTY_CLUE", savedClue.getId(), savedClue.getClueNo(),
                "生成处罚线索", operatorName, "ENFORCER",
                "超量开采自动生成处罚线索，来源申报单：" + declaration.getDeclarationNo() +
                        "，超采量：" + exceedVolume + "方", operatorId);

        return savedClue;
    }

    private BigDecimal calculatePenalty(BigDecimal exceedVolume) {
        BigDecimal unitPrice = new BigDecimal("50");
        return exceedVolume.multiply(unitPrice);
    }
}
