package com.river.sand.service;

import com.river.sand.common.BusinessException;
import com.river.sand.entity.*;
import com.river.sand.enums.*;
import com.river.sand.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RectificationReviewService {

    private final RectificationReviewRepository reviewRepository;
    private final OnsiteDetentionRepository detentionRepository;
    private final AuditService auditService;

    private final AtomicLong reviewCounter = new AtomicLong(0);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public RectificationReviewService(RectificationReviewRepository reviewRepository,
                                       OnsiteDetentionRepository detentionRepository,
                                       AuditService auditService) {
        this.reviewRepository = reviewRepository;
        this.detentionRepository = detentionRepository;
        this.auditService = auditService;
    }

    public List<RectificationReview> getReviewsByDetention(Long detentionId) {
        return reviewRepository.findByDetentionIdOrderByReviewTimeDesc(detentionId);
    }

    public List<RectificationReview> getReviewsByPenaltyClue(Long penaltyClueId) {
        return reviewRepository.findByPenaltyClueId(penaltyClueId);
    }

    public List<RectificationReview> getReviewsByReviewer(Long reviewerId) {
        return reviewRepository.findByReviewerId(reviewerId);
    }

    public Optional<RectificationReview> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Optional<RectificationReview> getReviewByNo(String reviewNo) {
        return reviewRepository.findByReviewNo(reviewNo);
    }

    @Transactional
    public RectificationReview createReview(RectificationReview review, Long reviewerId, String reviewerName) {
        OnsiteDetention detention = detentionRepository.findById(review.getDetentionId())
                .orElseThrow(() -> new BusinessException("暂扣记录不存在"));

        if (detention.getStatus() != DetentionStatus.RECTIFYING) {
            throw new BusinessException("当前状态不允许复查");
        }

        String reviewNo = "FG" + LocalDate.now().format(DATE_FORMAT) +
                String.format("%04d", reviewCounter.incrementAndGet());

        review.setReviewNo(reviewNo);
        review.setDetentionNo(detention.getDetentionNo());
        review.setReviewerId(reviewerId);
        review.setReviewerName(reviewerName);
        review.setReviewTime(LocalDateTime.now());

        if (review.getIsRectified() == null) {
            review.setIsRectified(false);
        }

        RectificationReview saved = reviewRepository.save(review);

        if (Boolean.TRUE.equals(review.getIsRectified())) {
            detention.setStatus(DetentionStatus.REVIEWED);
            detentionRepository.save(detention);
        }

        auditService.log("RECTIFICATION_REVIEW", saved.getId(), saved.getReviewNo(),
                "整改复查", reviewerName, "ENFORCER",
                "整改复查，复查结论：" + (Boolean.TRUE.equals(review.getIsRectified()) ? "整改合格" : "整改不合格") +
                        "，意见：" + review.getReviewOpinion(), reviewerId);

        return saved;
    }
}
