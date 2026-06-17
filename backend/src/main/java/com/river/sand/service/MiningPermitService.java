package com.river.sand.service;

import com.river.sand.common.BusinessException;
import com.river.sand.entity.MiningPermit;
import com.river.sand.enums.PermitStatus;
import com.river.sand.repository.MiningPermitRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MiningPermitService {

    private final MiningPermitRepository permitRepository;
    private final AuditService auditService;

    public MiningPermitService(MiningPermitRepository permitRepository,
                              AuditService auditService) {
        this.permitRepository = permitRepository;
        this.auditService = auditService;
    }

    public List<MiningPermit> getAllPermits(PermitStatus status, Long holderId) {
        if (status != null && holderId != null) {
            return permitRepository.findByHolderIdAndStatus(holderId, status);
        } else if (status != null) {
            return permitRepository.findByStatus(status);
        } else if (holderId != null) {
            return permitRepository.findByHolderId(holderId);
        }
        return permitRepository.findAll();
    }

    public List<MiningPermit> getPermitsByHolder(Long holderId) {
        return permitRepository.findByHolderId(holderId);
    }

    public List<MiningPermit> getPermitsByStatus(PermitStatus status) {
        return permitRepository.findByStatus(status);
    }

    public Optional<MiningPermit> getPermitById(Long id) {
        return permitRepository.findById(id);
    }

    public Optional<MiningPermit> getPermitByNo(String permitNo) {
        return permitRepository.findByPermitNo(permitNo);
    }

    public Optional<MiningPermit> getPermitByApplicationId(Long applicationId) {
        return permitRepository.findByApplicationId(applicationId);
    }

    @Transactional
    public void addUsedVolume(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        if (permit.getStatus() != PermitStatus.ACTIVE) {
            throw new BusinessException("许可证状态不允许录入采砂量");
        }

        if (permit.getEndDate().isBefore(LocalDate.now())) {
            throw new BusinessException("许可证已到期，不能补录采砂数据");
        }

        permit.setUsedVolume(permit.getUsedVolume().add(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    @Transactional
    public void freezeVolume(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        permit.setFrozenVolume(permit.getFrozenVolume().add(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    @Transactional
    public void unfreezeVolume(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        BigDecimal currentFrozen = permit.getFrozenVolume();
        if (volume.compareTo(currentFrozen) > 0) {
            volume = currentFrozen;
        }
        permit.setFrozenVolume(currentFrozen.subtract(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    @Transactional
    public void addPenaltyVolume(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        permit.setPenaltyVolume(permit.getPenaltyVolume().add(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    @Transactional
    public void reducePenaltyVolume(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        BigDecimal currentPenalty = permit.getPenaltyVolume();
        if (volume.compareTo(currentPenalty) > 0) {
            volume = currentPenalty;
        }
        permit.setPenaltyVolume(currentPenalty.subtract(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    @Transactional
    public void frozenToPenalty(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        BigDecimal currentFrozen = permit.getFrozenVolume();
        if (volume.compareTo(currentFrozen) > 0) {
            volume = currentFrozen;
        }
        permit.setFrozenVolume(currentFrozen.subtract(volume));
        permit.setPenaltyVolume(permit.getPenaltyVolume().add(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    @Transactional
    public void frozenToUsed(Long permitId, BigDecimal volume) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));

        BigDecimal currentFrozen = permit.getFrozenVolume();
        if (volume.compareTo(currentFrozen) > 0) {
            volume = currentFrozen;
        }
        permit.setFrozenVolume(currentFrozen.subtract(volume));
        permit.setUsedVolume(permit.getUsedVolume().add(volume));
        recalculateRemaining(permit);

        permitRepository.save(permit);
    }

    private void recalculateRemaining(MiningPermit permit) {
        BigDecimal used = permit.getUsedVolume();
        BigDecimal frozen = permit.getFrozenVolume();
        BigDecimal penalty = permit.getPenaltyVolume();
        BigDecimal total = permit.getPermittedVolume();

        BigDecimal remaining = total.subtract(used).subtract(frozen).subtract(penalty);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }
        permit.setRemainingVolume(remaining);
    }

    public BigDecimal getAvailableVolume(Long permitId) {
        MiningPermit permit = permitRepository.findById(permitId)
                .orElseThrow(() -> new BusinessException("许可证不存在"));
        return permit.getRemainingVolume();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkAndHandleExpiry() {
        LocalDate today = LocalDate.now();
        List<MiningPermit> permits = permitRepository.findByStatusAndEndDateBefore(
                PermitStatus.ACTIVE, today);

        for (MiningPermit permit : permits) {
            permit.setStatus(PermitStatus.EXPIRED);
            permitRepository.save(permit);

            auditService.log("PERMIT", permit.getId(), permit.getPermitNo(),
                    "许可证过期", "系统", "SYSTEM",
                    "许可证已自动过期", null);
        }
    }

    public boolean isExpired(MiningPermit permit) {
        return permit.getEndDate().isBefore(LocalDate.now());
    }

    public boolean canRecordMining(MiningPermit permit) {
        return permit.getStatus() == PermitStatus.ACTIVE &&
                !isExpired(permit);
    }
}
