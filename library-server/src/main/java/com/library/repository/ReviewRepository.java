package com.library.repository;

import com.library.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByBookIdAndParentIdIsNull(Long bookId, Pageable pageable);

    List<Review> findByRootIdOrderByCreatedAtAsc(Long rootId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :id AND r.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Review r SET r.replyCount = r.replyCount + 1 WHERE r.id = :id")
    void incrementReplyCount(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(r.likeCount), 0) FROM Review r WHERE r.userId = :userId")
    int sumLikeCountByUserId(@Param("userId") Long userId);
}
