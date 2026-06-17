package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.MiningApplication;
import com.river.sand.enums.ApplicationStatus;
import com.river.sand.service.MiningApplicationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final MiningApplicationService applicationService;

    public ApplicationController(MiningApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public Result<List<MiningApplication>> getAllApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long applicantId) {
        return Result.success(applicationService.getAllApplications(status, applicantId));
    }

    @GetMapping("/{id}")
    public Result<MiningApplication> getApplicationById(@PathVariable Long id) {
        return applicationService.getApplicationById(id)
                .map(Result::success)
                .orElse(Result.error("申请不存在"));
    }

    @GetMapping("/no/{applicationNo}")
    public Result<MiningApplication> getApplicationByNo(@PathVariable String applicationNo) {
        return applicationService.getApplicationByNo(applicationNo)
                .map(Result::success)
                .orElse(Result.error("申请不存在"));
    }

    @PostMapping
    public Result<MiningApplication> createApplication(
            @RequestBody MiningApplication application,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username) {
        return Result.success(applicationService.createApplication(application, userId, username));
    }

    @PostMapping("/{id}/submit")
    public Result<MiningApplication> submitApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username) {
        return Result.success(applicationService.submitApplication(id, userId, username));
    }

    @PostMapping("/{id}/review")
    public Result<MiningApplication> reviewApplication(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewData,
            @RequestHeader("X-User-Id") Long reviewerId,
            @RequestHeader("X-User-Name") String reviewerName) {
        boolean approved = Boolean.TRUE.equals(reviewData.get("approved"));
        String opinion = (String) reviewData.get("opinion");
        return Result.success(applicationService.reviewApplication(
                id, approved, opinion, reviewerId, reviewerName));
    }

    @PutMapping("/{id}/change")
    public Result<MiningApplication> changeApplication(
            @PathVariable Long id,
            @RequestBody MiningApplication application,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username) {
        return Result.success(applicationService.changeApplication(id, application, userId, username));
    }

    @PostMapping("/{id}/suspend")
    public Result<Void> suspendApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> suspendData,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username) {
        String reason = suspendData.get("reason");
        applicationService.suspendApplication(id, reason, userId, username);
        return Result.success();
    }

    @PostMapping("/{id}/resume")
    public Result<Void> resumeApplication(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username) {
        applicationService.resumeApplication(id, userId, username);
        return Result.success();
    }

    @GetMapping("/{enterpriseId}/violations")
    public Result<List<MiningApplication>> getHistoricalViolations(
            @PathVariable Long enterpriseId) {
        return Result.success(applicationService.getHistoricalViolations(enterpriseId));
    }
}
