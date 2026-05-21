package com.library.repository;

import com.library.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    Optional<ReviewLike> findByReviewIdAndUserId(Long reviewId, Long userId);

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    List<ReviewLike> findByUserIdAndReviewIdIn(Long userId, List<Long> reviewIds);
}
