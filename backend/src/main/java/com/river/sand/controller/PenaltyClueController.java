package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.PenaltyClue;
import com.river.sand.enums.PenaltyStatus;
import com.river.sand.service.PenaltyClueService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/penalty-clues")
public class PenaltyClueController {

    private final PenaltyClueService penaltyClueService;

    public PenaltyClueController(PenaltyClueService penaltyClueService) {
        this.penaltyClueService = penaltyClueService;
    }

    @GetMapping
    public Result<List<PenaltyClue>> getAllClues() {
        return Result.success(penaltyClueService.getAllClues());
    }

    @GetMapping("/status/{status}")
    public Result<List<PenaltyClue>> getCluesByStatus(@PathVariable PenaltyStatus status) {
        return Result.success(penaltyClueService.getCluesByStatus(status));
    }

    @GetMapping("/permit/{permitId}")
    public Result<List<PenaltyClue>> getCluesByPermit(@PathVariable Long permitId) {
        return Result.success(penaltyClueService.getCluesByPermit(permitId));
    }

    @GetMapping("/enterprise/{enterpriseId}")
    public Result<List<PenaltyClue>> getCluesByEnterprise(@PathVariable Long enterpriseId) {
        return Result.success(penaltyClueService.getCluesByEnterprise(enterpriseId));
    }

    @GetMapping("/{id}")
    public Result<PenaltyClue> getClueById(@PathVariable Long id) {
        return penaltyClueService.getClueById(id)
                .map(Result::success)
                .orElse(Result.error("处罚线索不存在"));
    }

    @PostMapping("/{id}/submit")
    public Result<PenaltyClue> submitForReview(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long operatorId,
            @RequestHeader("X-User-Name") String operatorName) {
        return Result.success(penaltyClueService.submitForReview(id, operatorId, operatorName));
    }

    @PostMapping("/{id}/review")
    public Result<PenaltyClue> review(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewData,
            @RequestHeader("X-User-Id") Long reviewerId,
            @RequestHeader("X-User-Name") String reviewerName) {
        boolean approved = Boolean.TRUE.equals(reviewData.get("approved"));
        String opinion = (String) reviewData.get("opinion");
        return Result.success(penaltyClueService.review(id, approved, opinion, reviewerId, reviewerName));
    }

    @PostMapping("/{id}/process")
    public Result<PenaltyClue> processClue(
            @PathVariable Long id,
            @RequestBody Map<String, String> processData,
            @RequestHeader("X-User-Id") Long handlerId,
            @RequestHeader("X-User-Name") String handlerName) {
        String handleResult = processData.get("handleResult");
        return Result.success(penaltyClueService.processClue(id, handleResult, handlerId, handlerName));
    }
}
