package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.OnsiteDetention;
import com.river.sand.enums.DetentionStatus;
import com.river.sand.service.OnsiteDetentionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/detentions")
public class DetentionController {

    private final OnsiteDetentionService detentionService;

    public DetentionController(OnsiteDetentionService detentionService) {
        this.detentionService = detentionService;
    }

    @GetMapping
    public Result<List<OnsiteDetention>> getDetections(
            @RequestParam(required = false) Long permitId,
            @RequestParam(required = false) Long declarationId,
            @RequestParam(required = false) Long officerId,
            @RequestParam(required = false) DetentionStatus status) {
        if (permitId != null) {
            return Result.success(detentionService.getDetectionsByPermit(permitId));
        }
        if (declarationId != null) {
            return Result.success(detentionService.getDetectionsByDeclaration(declarationId));
        }
        if (officerId != null) {
            return Result.success(detentionService.getDetectionsByOfficer(officerId));
        }
        if (status != null) {
            return Result.success(detentionService.getDetectionsByStatus(status));
        }
        return Result.error("请提供查询条件");
    }

    @GetMapping("/{id}")
    public Result<OnsiteDetention> getDetentionById(@PathVariable Long id) {
        return detentionService.getDetentionById(id)
                .map(Result::success)
                .orElse(Result.error("暂扣记录不存在"));
    }

    @GetMapping("/no/{detentionNo}")
    public Result<OnsiteDetention> getDetentionByNo(@PathVariable String detentionNo) {
        return detentionService.getDetentionByNo(detentionNo)
                .map(Result::success)
                .orElse(Result.error("暂扣记录不存在"));
    }

    @PostMapping
    public Result<OnsiteDetention> createDetention(
            @RequestBody OnsiteDetention detention,
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Name") String officerName) {
        return Result.success(detentionService.createDetention(detention, officerId, officerName));
    }

    @PostMapping("/{id}/start-rectification")
    public Result<OnsiteDetention> startRectification(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Name") String officerName) {
        String requirement = data.get("requirement");
        return Result.success(detentionService.startRectification(id, requirement, officerId, officerName));
    }

    @PostMapping("/{id}/release")
    public Result<OnsiteDetention> releaseDetention(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Name") String officerName) {
        String reason = data.get("reason");
        return Result.success(detentionService.releaseDetention(id, reason, officerId, officerName));
    }

    @PostMapping("/{id}/confirm-violation")
    public Result<OnsiteDetention> confirmViolation(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            @RequestHeader("X-User-Id") Long officerId,
            @RequestHeader("X-User-Name") String officerName) {
        String reason = data.get("reason");
        return Result.success(detentionService.confirmViolation(id, reason, officerId, officerName));
    }
}
