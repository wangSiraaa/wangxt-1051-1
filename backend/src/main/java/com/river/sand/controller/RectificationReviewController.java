package com.river.sand.controller;

import com.river.sand.common.Result;
import com.river.sand.entity.RectificationReview;
import com.river.sand.service.RectificationReviewService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rectification-reviews")
public class RectificationReviewController {

    private final RectificationReviewService reviewService;

    public RectificationReviewController(RectificationReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public Result<List<RectificationReview>> getReviews(
            @RequestParam(required = false) Long detentionId,
            @RequestParam(required = false) Long penaltyClueId,
            @RequestParam(required = false) Long reviewerId) {
        if (detentionId != null) {
            return Result.success(reviewService.getReviewsByDetention(detentionId));
        }
        if (penaltyClueId != null) {
            return Result.success(reviewService.getReviewsByPenaltyClue(penaltyClueId));
        }
        if (reviewerId != null) {
            return Result.success(reviewService.getReviewsByReviewer(reviewerId));
        }
        return Result.error("请提供查询条件");
    }

    @GetMapping("/{id}")
    public Result<RectificationReview> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(Result::success)
                .orElse(Result.error("复查记录不存在"));
    }

    @GetMapping("/no/{reviewNo}")
    public Result<RectificationReview> getReviewByNo(@PathVariable String reviewNo) {
        return reviewService.getReviewByNo(reviewNo)
                .map(Result::success)
                .orElse(Result.error("复查记录不存在"));
    }

    @PostMapping
    public Result<RectificationReview> createReview(
            @RequestBody RectificationReview review,
            @RequestHeader("X-User-Id") Long reviewerId,
            @RequestHeader("X-User-Name") String reviewerName) {
        return Result.success(reviewService.createReview(review, reviewerId, reviewerName));
    }
}
