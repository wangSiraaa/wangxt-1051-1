package com.river.sand.service;

import com.river.sand.common.BusinessException;
import com.river.sand.entity.MiningApplication;
import com.river.sand.entity.MiningPermit;
import com.river.sand.entity.User;
import com.river.sand.enums.ApplicationStatus;
import com.river.sand.enums.PermitStatus;
import com.river.sand.repository.MiningApplicationRepository;
import com.river.sand.repository.MiningPermitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MiningApplicationService {

    private final MiningApplicationRepository applicationRepository;
    private final MiningPermitRepository permitRepository;
    private final ForbiddenPeriodService forbiddenPeriodService;
    private final AnnualQuotaService annualQuotaService;
    private final AuditService auditService;
    private final MasterDataService masterDataService;

    private final AtomicLong applicationCounter = new AtomicLong(0);
    private final AtomicLong permitCounter = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public MiningApplicationService(MiningApplicationRepository applicationRepository,
                                   MiningPermitRepository permitRepository,
                                   ForbiddenPeriodService forbiddenPeriodService,
                                   AnnualQuotaService annualQuotaService,
                                   AuditService auditService,
                                   MasterDataService masterDataService) {
        this.applicationRepository = applicationRepository;
        this.permitRepository = permitRepository;
        this.forbiddenPeriodService = forbiddenPeriodService;
        this.annualQuotaService = annualQuotaService;
        this.auditService = auditService;
        this.masterDataService = masterDataService;
    }

    public List<MiningApplication> getAllApplications(ApplicationStatus status, Long applicantId) {
        if (status != null && applicantId != null) {
            return applicationRepository.findByApplicantIdAndStatus(applicantId, status);
        } else if (status != null) {
            return applicationRepository.findByStatus(status);
        } else if (applicantId != null) {
            return applicationRepository.findByApplicantId(applicantId);
        }
        return applicationRepository.findAll();
    }

    public Optional<MiningApplication> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public Optional<MiningApplication> getApplicationByNo(String applicationNo) {
        return applicationRepository.findByApplicationNo(applicationNo);
    }

    @Transactional
    public MiningApplication createApplication(MiningApplication application, Long userId, String username) {
        String appNo = "SQ" + LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", applicationCounter.incrementAndGet());
        application.setApplicationNo(appNo);
        application.setStatus(ApplicationStatus.DRAFT);
        application.setApplicantId(userId);
        application.setCreateBy(username);
        MiningApplication saved = applicationRepository.save(application);

        auditService.log("APPLICATION", saved.getId(), saved.getApplicationNo(),
                "创建申请", username, "ENTERPRISE",
                "创建采砂许可申请，申请编号：" + saved.getApplicationNo(), userId);

        return saved;
    }

    @Transactional
    public MiningApplication submitApplication(Long id, Long userId, String username) {
        MiningApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.DRAFT &&
                application.getStatus() != ApplicationStatus.RETURNED) {
            throw new BusinessException("当前状态不允许提交");
        }

        if (forbiddenPeriodService.hasForbiddenPeriodOverlap(
                application.getStartDate(), application.getEndDate())) {
            throw new BusinessException("申请时段包含禁采期，不允许提交");
        }

        application.setStatus(ApplicationStatus.PENDING_REVIEW);
        MiningApplication saved = applicationRepository.save(application);

        auditService.log("APPLICATION", saved.getId(), saved.getApplicationNo(),
                "提交申请", username, "ENTERPRISE",
                "提交采砂许可申请，进入审核流程", userId);

        return saved;
    }

    @Transactional
    public MiningApplication reviewApplication(Long id, boolean approved, String opinion,
                                          Long reviewerId, String reviewerName) {
        MiningApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.PENDING_REVIEW) {
            throw new BusinessException("当前状态不允许审核");
        }

        application.setReviewOpinion(opinion);
        application.setReviewerId(reviewerId);
        application.setReviewTime(LocalDateTime.now());

        if (approved) {
            if (!annualQuotaService.hasSufficientQuota(
                    application.getStartDate().getYear(),
                    application.getRiverSectionId(),
                    application.getEstimatedVolume())) {
                throw new BusinessException("年度采砂额度不足，无法通过审核");
            }

            List<ApplicationStatus> conflictStatuses = List.of(
                    ApplicationStatus.APPROVED, ApplicationStatus.CHANGED);
            List<MiningApplication> conflicts = applicationRepository
                    .findByRiverSectionIdAndStatusIn(
                            application.getRiverSectionId(), conflictStatuses);
            for (MiningApplication app : conflicts) {
                if (!app.getId().equals(application.getId()) &&
                        isDateOverlap(app.getStartDate(), app.getEndDate(),
                                application.getStartDate(), application.getEndDate())) {
                    throw new BusinessException("同河段存在冲突的许可申请");
                }
            }

            List<MiningPermit> permitConflicts = permitRepository
                    .findByRiverSectionIdAndStatusIn(
                            application.getRiverSectionId(),
                            List.of(PermitStatus.ACTIVE));
            for (MiningPermit permit : permitConflicts) {
                if (isDateOverlap(permit.getStartDate(), permit.getEndDate(),
                        application.getStartDate(), application.getEndDate())) {
                    throw new BusinessException("同河段存在冲突的有效许可证");
                }
            }

            application.setStatus(ApplicationStatus.APPROVED);
            MiningApplication savedApp = applicationRepository.save(application);

            annualQuotaService.consumeQuota(
                    application.getStartDate().getYear(),
                    application.getRiverSectionId(),
                    application.getEstimatedVolume());

            createPermit(application, reviewerId, reviewerName);

            auditService.log("APPLICATION", savedApp.getId(), savedApp.getApplicationNo(),
                    "审核通过", reviewerName, "AUDITOR",
                    "审核通过，意见：" + opinion, reviewerId);

            return savedApp;
        } else {
            application.setStatus(ApplicationStatus.RETURNED);
            MiningApplication saved = applicationRepository.save(application);

            auditService.log("APPLICATION", saved.getId(), saved.getApplicationNo(),
                    "审核退回", reviewerName, "AUDITOR",
                    "审核退回，意见：" + opinion, reviewerId);

            return saved;
        }
    }

    private void createPermit(MiningApplication application, Long reviewerId, String reviewerName) {
        MiningPermit permit = new MiningPermit();
        String permitNo = "XK" + LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", permitCounter.incrementAndGet());
        permit.setPermitNo(permitNo);
        permit.setApplicationId(application.getId());
        permit.setHolderId(application.getApplicantId());
        permit.setHolderName(application.getEnterpriseName());
        permit.setRiverSectionId(application.getRiverSectionId());
        permit.setRiverSectionName(application.getRiverSectionName());
        permit.setVesselIds(application.getVesselIds());
        permit.setVesselNames(application.getVesselNames());
        permit.setStartDate(application.getStartDate());
        permit.setEndDate(application.getEndDate());
        permit.setPermittedVolume(application.getEstimatedVolume());
        permit.setDepositAmount(application.getDepositAmount());
        permit.setStatus(PermitStatus.ACTIVE);
        permit.setIssueDate(LocalDateTime.now());

        MiningPermit savedPermit = permitRepository.save(permit);

        auditService.log("PERMIT", savedPermit.getId(), savedPermit.getPermitNo(),
                "发放许可证", reviewerName, "AUDITOR",
                "许可证已发放，许可编号：" + savedPermit.getPermitNo(), reviewerId);
    }

    @Transactional
    public MiningApplication changeApplication(Long id, MiningApplication updateApp, Long userId, String username) {
        MiningApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.APPROVED &&
                application.getStatus() != ApplicationStatus.CHANGED) {
            throw new BusinessException("当前状态不允许变更");
        }

        MiningPermit permit = permitRepository.findByApplicationId(id)
                .orElseThrow(() -> new BusinessException("关联许可证不存在"));

        if (permit.getStatus() != PermitStatus.ACTIVE) {
            throw new BusinessException("许可证状态不允许变更");
        }

        if (forbiddenPeriodService.hasForbiddenPeriodOverlap(
                updateApp.getStartDate(), updateApp.getEndDate())) {
            throw new BusinessException("变更时段包含禁采期，不允许变更");
        }

        BigDecimal oldVolume = application.getEstimatedVolume();
        BigDecimal newVolume = updateApp.getEstimatedVolume();
        if (newVolume.compareTo(oldVolume) > 0) {
            BigDecimal diff = newVolume.subtract(oldVolume);
            if (!annualQuotaService.hasSufficientQuota(
                    application.getStartDate().getYear(),
                    application.getRiverSectionId(), diff)) {
                throw new BusinessException("年度额度不足，无法增加许可量");
            }
            annualQuotaService.consumeQuota(
                    application.getStartDate().getYear(),
                    application.getRiverSectionId(), diff);
        } else if (newVolume.compareTo(oldVolume) < 0) {
            BigDecimal diff = oldVolume.subtract(newVolume);
            annualQuotaService.releaseQuota(
                    application.getStartDate().getYear(),
                    application.getRiverSectionId(), diff);
        }

        application.setStartDate(updateApp.getStartDate());
        application.setEndDate(updateApp.getEndDate());
        application.setEstimatedVolume(updateApp.getEstimatedVolume());
        application.setVesselIds(updateApp.getVesselIds());
        application.setVesselNames(updateApp.getVesselNames());
        application.setStatus(ApplicationStatus.CHANGED);
        application.setChangeDescription(updateApp.getRemark());
        MiningApplication savedApp = applicationRepository.save(application);

        permit.setStartDate(updateApp.getStartDate());
        permit.setEndDate(updateApp.getEndDate());
        permit.setPermittedVolume(updateApp.getEstimatedVolume());
        permit.setRemainingVolume(updateApp.getEstimatedVolume().subtract(permit.getUsedVolume()));
        permit.setVesselIds(updateApp.getVesselIds());
        permit.setVesselNames(updateApp.getVesselNames());
        permit.setChangeDescription(updateApp.getRemark());
        permitRepository.save(permit);

        auditService.log("APPLICATION", savedApp.getId(), savedApp.getApplicationNo(),
                "变更申请", username, "ENTERPRISE",
                "许可信息已变更，变更内容：" + updateApp.getRemark(), userId);

        auditService.log("PERMIT", permit.getId(), permit.getPermitNo(),
                "许可证变更", username, "ENTERPRISE",
                "许可证信息已变更，变更内容：" + updateApp.getRemark(), userId);

        return savedApp;
    }

    @Transactional
    public void suspendApplication(Long id, String reason, Long userId, String username) {
        MiningApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.APPROVED &&
                application.getStatus() != ApplicationStatus.CHANGED) {
            throw new BusinessException("当前状态不允许暂停");
        }

        MiningPermit permit = permitRepository.findByApplicationId(id)
                .orElseThrow(() -> new BusinessException("关联许可证不存在"));

        application.setStatus(ApplicationStatus.SUSPENDED);
        applicationRepository.save(application);

        permit.setStatus(PermitStatus.SUSPENDED);
        permit.setSuspendReason(reason);
        permit.setSuspendTime(LocalDateTime.now());
        permitRepository.save(permit);

        auditService.log("APPLICATION", application.getId(), application.getApplicationNo(),
                "暂停许可", username, "AUDITOR",
                "许可已暂停，原因：" + reason, userId);

        auditService.log("PERMIT", permit.getId(), permit.getPermitNo(),
                "许可证暂停", username, "AUDITOR",
                "许可证已暂停，原因：" + reason, userId);
    }

    @Transactional
    public void resumeApplication(Long id, Long userId, String username) {
        MiningApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));

        if (application.getStatus() != ApplicationStatus.SUSPENDED) {
            throw new BusinessException("当前状态不允许恢复");
        }

        MiningPermit permit = permitRepository.findByApplicationId(id)
                .orElseThrow(() -> new BusinessException("关联许可证不存在"));

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(application);

        permit.setStatus(PermitStatus.ACTIVE);
        permit.setResumeTime(LocalDateTime.now());
        permitRepository.save(permit);

        auditService.log("APPLICATION", application.getId(), application.getApplicationNo(),
                "恢复许可", username, "AUDITOR",
                "许可已恢复", userId);

        auditService.log("PERMIT", permit.getId(), permit.getPermitNo(),
                "许可证恢复", username, "AUDITOR",
                "许可证已恢复", userId);
    }

    public List<MiningApplication> getHistoricalViolations(Long enterpriseId) {
        List<MiningApplication> all = applicationRepository.findByApplicantId(enterpriseId);
        List<MiningApplication> violations = new ArrayList<>();
        for (MiningApplication app : all) {
            if (app.getStatus() == ApplicationStatus.RETURNED ||
                    app.getStatus() == ApplicationStatus.CANCELLED) {
                violations.add(app);
            }
        }
        return violations;
    }

    private boolean isDateOverlap(LocalDate start1, LocalDate end1,
                              LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }
}
