package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.AnnualQuota;
import com.river.sand.entity.ForbiddenPeriod;
import com.river.sand.service.AnnualQuotaService;
import com.river.sand.service.ForbiddenPeriodService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quota")
public class QuotaController {

    private final AnnualQuotaService annualQuotaService;
    private final ForbiddenPeriodService forbiddenPeriodService;

    public QuotaController(AnnualQuotaService annualQuotaService,
                          ForbiddenPeriodService forbiddenPeriodService) {
        this.annualQuotaService = annualQuotaService;
        this.forbiddenPeriodService = forbiddenPeriodService;
    }

    @GetMapping
    public Result<List<AnnualQuota>> getAllQuotas() {
        return Result.success(annualQuotaService.getAllQuotas());
    }

    @GetMapping("/year/{year}")
    public Result<List<AnnualQuota>> getQuotasByYear(@PathVariable Integer year) {
        return Result.success(annualQuotaService.getQuotasByYear(year));
    }

    @GetMapping("/{year}/{riverSectionId}")
    public Result<AnnualQuota> getQuota(@PathVariable Integer year,
                                        @PathVariable Long riverSectionId,
                                        @RequestParam(required = false) String riverSectionName) {
        return Result.success(annualQuotaService.getOrCreateQuota(
                year, riverSectionId, riverSectionName));
    }

    @GetMapping("/remaining/{year}/{riverSectionId}")
    public Result<BigDecimal> getRemainingQuota(@PathVariable Integer year,
                                               @PathVariable Long riverSectionId) {
        return Result.success(annualQuotaService.getRemainingQuota(year, riverSectionId));
    }

    @GetMapping("/forbidden-periods")
    public Result<List<ForbiddenPeriod>> getForbiddenPeriods() {
        return Result.success(forbiddenPeriodService.getAllEnabledPeriods());
    }

    @GetMapping("/check-forbidden")
    public Result<Map<String, Object>> checkForbiddenPeriod(
            @RequestParam LocalDate date) {
        Map<String, Object> result = new HashMap<>();
        result.put("isForbidden", forbiddenPeriodService.isInForbiddenPeriod(date));
        ForbiddenPeriod period = forbiddenPeriodService.getForbiddenPeriodForDate(date);
        result.put("period", period);
        return Result.success(result);
    }

    @GetMapping("/check-overlap")
    public Result<Map<String, Object>> checkForbiddenOverlap(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasOverlap", forbiddenPeriodService.hasForbiddenPeriodOverlap(startDate, endDate));
        return Result.success(result);
    }
}
