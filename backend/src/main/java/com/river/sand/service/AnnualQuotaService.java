package com.river.sand.service;

import com.river.sand.entity.AnnualQuota;
import com.river.sand.repository.AnnualQuotaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AnnualQuotaService {

    private final AnnualQuotaRepository annualQuotaRepository;

    @Value("${app.annual-quota-default:100000}")
    private BigDecimal defaultQuota;

    public AnnualQuotaService(AnnualQuotaRepository annualQuotaRepository) {
        this.annualQuotaRepository = annualQuotaRepository;
    }

    public List<AnnualQuota> getAllQuotas() {
        return annualQuotaRepository.findAll();
    }

    public List<AnnualQuota> getQuotasByYear(Integer year) {
        return annualQuotaRepository.findByYear(year);
    }

    public Optional<AnnualQuota> getQuotaByYearAndSection(Integer year, Long riverSectionId) {
        return annualQuotaRepository.findByYearAndRiverSectionId(year, riverSectionId);
    }

    public AnnualQuota getOrCreateQuota(Integer year, Long riverSectionId, String riverSectionName) {
        return annualQuotaRepository.findByYearAndRiverSectionId(year, riverSectionId)
                .orElseGet(() -> {
                    AnnualQuota quota = new AnnualQuota();
                    quota.setYear(year);
                    quota.setRiverSectionId(riverSectionId);
                    quota.setRiverSectionName(riverSectionName);
                    quota.setTotalQuota(defaultQuota);
                    quota.setUsedQuota(BigDecimal.ZERO);
                    quota.setRemainingQuota(defaultQuota);
                    return annualQuotaRepository.save(quota);
                });
    }

    @Transactional
    public void consumeQuota(Integer year, Long riverSectionId, BigDecimal amount) {
        AnnualQuota quota = getOrCreateQuota(year, riverSectionId, null);
        if (quota.getRemainingQuota().compareTo(amount) < 0) {
            throw new RuntimeException("年度额度不足，剩余：" + quota.getRemainingQuota() + "，需要：" + amount);
        }
        quota.setUsedQuota(quota.getUsedQuota().add(amount));
        quota.setRemainingQuota(quota.getTotalQuota().subtract(quota.getUsedQuota()));
        annualQuotaRepository.save(quota);
    }

    @Transactional
    public void releaseQuota(Integer year, Long riverSectionId, BigDecimal amount) {
        AnnualQuota quota = getOrCreateQuota(year, riverSectionId, null);
        quota.setUsedQuota(quota.getUsedQuota().subtract(amount));
        if (quota.getUsedQuota().compareTo(BigDecimal.ZERO) < 0) {
            quota.setUsedQuota(BigDecimal.ZERO);
        }
        quota.setRemainingQuota(quota.getTotalQuota().subtract(quota.getUsedQuota()));
        annualQuotaRepository.save(quota);
    }

    public boolean hasSufficientQuota(Integer year, Long riverSectionId, BigDecimal amount) {
        AnnualQuota quota = getOrCreateQuota(year, riverSectionId, null);
        return quota.getRemainingQuota().compareTo(amount) >= 0;
    }

    public BigDecimal getRemainingQuota(Integer year, Long riverSectionId) {
        AnnualQuota quota = getOrCreateQuota(year, riverSectionId, null);
        return quota.getRemainingQuota();
    }
}
