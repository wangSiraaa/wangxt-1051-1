package com.river.sand.repository;

import com.river.sand.entity.RectificationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RectificationReviewRepository extends JpaRepository<RectificationReview, Long> {
    Optional<RectificationReview> findByReviewNo(String reviewNo);
    List<RectificationReview> findByDetentionId(Long detentionId);
    List<RectificationReview> findByDetentionIdOrderByReviewTimeDesc(Long detentionId);
    List<RectificationReview> findByPenaltyClueId(Long penaltyClueId);
    List<RectificationReview> findByReviewerId(Long reviewerId);
}
