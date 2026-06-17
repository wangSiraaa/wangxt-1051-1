package com.river.sand.service;

import com.river.sand.common.BusinessException;
import com.river.sand.entity.MiningPermit;
import com.river.sand.entity.OnsiteInspection;
import com.river.sand.entity.PenaltyClue;
import com.river.sand.entity.User;
import com.river.sand.enums.InspectionResult;
import com.river.sand.enums.PenaltyStatus;
import com.river.sand.enums.PermitStatus;
import com.river.sand.enums.Role;
import com.river.sand.repository.MiningPermitRepository;
import com.river.sand.repository.OnsiteInspectionRepository;
import com.river.sand.repository.PenaltyClueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OnsiteInspectionService {

    private final OnsiteInspectionRepository inspectionRepository;
    private final MiningPermitRepository permitRepository;
    private final MiningPermitService permitService;
    private final PenaltyClueRepository penaltyClueRepository;
    private final AuditService auditService;
    private final MasterDataService masterDataService;

    private final AtomicLong inspectionCounter = new AtomicLong(0);
    private final AtomicLong clueCounter = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public OnsiteInspectionService(OnsiteInspectionRepository inspectionRepository,
                                  MiningPermitRepository permitRepository,
                                  MiningPermitService permitService,
                                  PenaltyClueRepository penaltyClueRepository,
                                  AuditService auditService,
                                  MasterDataService masterDataService) {
        this.inspectionRepository = inspectionRepository;
        this.permitRepository = permitRepository;
        this.permitService = permitService;
        this.penaltyClueRepository = penaltyClueRepository;
        this.auditService = auditService;
        this.masterDataService = masterDataService;
    }

    public List<OnsiteInspection> getAllInspections() {
        return inspectionRepository.findAll();
    }

    public List<OnsiteInspection> getInspectionsByPermit(Long permitId) {
        return inspectionRepository.findByPermitId(permitId);
    }

    public List<OnsiteInspection> getInspectionsByInspector(Long inspectorId) {
        return inspectionRepository.findByInspectorId(inspectorId);
    }

    public Optional<OnsiteInspection> getInspectionById(Long id) {
        return inspectionRepository.findById(id);
    }

    @Transactional
    public OnsiteInspection createInspection(OnsiteInspection inspection,
                                            Long inspectorId, String inspectorName) {
        MiningPermit permit = permitRepository.findById(inspection.getPermitId())
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        if (permit.getStatus() != PermitStatus.ACTIVE) {
            throw new BusinessException("许可证状态不允许现场核查");
        }

        if (permit.getEndDate().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("许可证已到期，不能进行现场核查");
        }

        permit.setPermitNo(permit.getPermitNo());
        inspection.setPermitNo(permit.getPermitNo());
        inspection.setInspectorId(inspectorId);
        inspection.setInspectorName(inspectorName);
        inspection.setInspectionTime(LocalDateTime.now());

        BigDecimal totalUsed = permit.getUsedVolume().add(inspection.getActualVolume());
        if (totalUsed.compareTo(permit.getPermittedVolume()) > 0) {
            inspection.setResult(InspectionResult.ABNORMAL);
            inspection.setAbnormalDescription("超许可方量开采，超采量：" +
                    totalUsed.subtract(permit.getPermittedVolume()) + "方");
        } else if (inspection.getResult() == null) {
            inspection.setResult(InspectionResult.NORMAL);
        }

        OnsiteInspection saved = inspectionRepository.save(inspection);

        permitService.addUsedVolume(permit.getId(), inspection.getActualVolume());

        BigDecimal totalUsedAfter = permit.getUsedVolume();
        if (totalUsedAfter.compareTo(permit.getPermittedVolume()) > 0) {
            BigDecimal exceedVolume = totalUsedAfter.subtract(permit.getPermittedVolume());
            createPenaltyClue(permit, saved, exceedVolume, inspectorId, inspectorName);
        }

        auditService.log("INSPECTION", saved.getId(),
                "HC" + saved.getId(),
                "现场核查", inspectorName, "ENFORCER",
                "现场核查完成，核查结果：" + inspection.getResult().getDescription() +
                        "，实际方量：" + inspection.getActualVolume(), inspectorId);

        auditService.log("PERMIT", permit.getId(), permit.getPermitNo(),
                "采砂量录入", inspectorName, "ENFORCER",
                "现场核查录入采砂量：" + inspection.getActualVolume() + "方", inspectorId);

        return saved;
    }

    private void createPenaltyClue(MiningPermit permit, OnsiteInspection inspection,
                                BigDecimal exceedVolume, Long operatorId, String operatorName) {
        PenaltyClue clue = new PenaltyClue();
        String clueNo = "XZ" + java.time.LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", clueCounter.incrementAndGet());
        clue.setClueNo(clueNo);
        clue.setClueType("超许可方量");
        clue.setClueSource("现场核查");
        clue.setPermitId(permit.getId());
        clue.setPermitNo(permit.getPermitNo());
        clue.setInspectionId(inspection.getId());
        clue.setEnterpriseId(permit.getHolderId());
        clue.setEnterpriseName(permit.getHolderName());
        clue.setDescription("现场核查发现超许可方量开采，许可量：" +
                permit.getPermittedVolume() + "方，累计采量：" +
                permit.getUsedVolume() + "方，超采量：" + exceedVolume + "方");
        clue.setExceedVolume(exceedVolume);
        clue.setPenaltyAmount(calculatePenalty(exceedVolume));
        clue.setStatus(PenaltyStatus.PENDING);

        PenaltyClue savedClue = penaltyClueRepository.save(clue);

        auditService.log("PENALTY_CLUE", savedClue.getId(), savedClue.getClueNo(),
                "生成处罚线索", operatorName, "ENFORCER",
                "超量开采自动生成处罚线索，超采量：" + exceedVolume + "方", operatorId);
    }

    private BigDecimal calculatePenalty(BigDecimal exceedVolume) {
        BigDecimal unitPrice = new BigDecimal("50");
        return exceedVolume.multiply(unitPrice);
    }
}
