package com.library.service;

import com.library.dto.*;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.repository.ReviewLikeRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewLikeRepository reviewLikeRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
    }

    public PageResult<ReviewResponse> getReviews(Long bookId, String sort,
                                                  int page, int size, Long currentUserId) {
        Sort sortObj;
        if ("hot".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page - 1, size, sortObj);
        Page<Review> reviewPage = reviewRepository.findByBookIdAndParentIdIsNull(bookId, pageable);

        List<Long> reviewIds = reviewPage.getContent().stream()
            .map(Review::getId).collect(Collectors.toList());

        Map<Long, List<Review>> repliesMap = new HashMap<>();
        for (Long id : reviewIds) {
            List<Review> replies = reviewRepository.findByRootIdOrderByCreatedAtAsc(id);
            repliesMap.put(id, replies);
        }

        Set<Long> allReviewIds = new HashSet<>(reviewIds);
        for (List<Review> replies : repliesMap.values()) {
            for (Review r : replies) {
                allReviewIds.add(r.getId());
            }
        }

        Set<Long> likedIds = Collections.emptySet();
        if (currentUserId != null) {
            likedIds = reviewLikeRepository
                .findByUserIdAndReviewIdIn(currentUserId, new ArrayList<>(allReviewIds))
                .stream().map(l -> l.getReviewId()).collect(Collectors.toSet());
        }

        Map<Long, String> usernameCache = new HashMap<>();
        final Set<Long> finalLikedIds = likedIds;

        List<ReviewResponse> records = reviewPage.getContent().stream()
            .map(r -> toResponse(r, repliesMap, finalLikedIds, usernameCache))
            .collect(Collectors.toList());

        return PageResult.<ReviewResponse>builder()
            .records(records)
            .total(reviewPage.getTotalElements())
            .page(page)
            .size(size)
            .build();
    }

    @Transactional
    public ReviewResponse createReview(Long bookId, Long userId, ReviewRequest req) {
        Review review = Review.builder()
            .bookId(bookId).userId(userId).content(req.getContent())
            .likeCount(0).replyCount(0)
            .build();
        review = reviewRepository.save(review);
        return toResponse(review, Collections.emptyMap(), Collections.emptySet(), new HashMap<>());
    }

    @Transactional
    public ReviewResponse createReply(Long parentId, Long userId, ReviewRequest req) {
        Review parent = reviewRepository.findById(parentId)
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + parentId));

        Long rootId = parent.getParentId() == null ? parentId : parent.getRootId();

        Review reply = Review.builder()
            .bookId(parent.getBookId()).userId(userId)
            .parentId(parentId).rootId(rootId)
            .content(req.getContent()).likeCount(0).replyCount(0)
            .build();
        reply = reviewRepository.save(reply);

        reviewRepository.incrementReplyCount(rootId);
        return toResponse(reply, Collections.emptyMap(), Collections.emptySet(), new HashMap<>());
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, boolean isAdmin,
                                        ReviewRequest req) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + reviewId));

        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new SecurityException("无权编辑他人书评");
        }

        long minutes = ChronoUnit.MINUTES.between(review.getCreatedAt(), LocalDateTime.now());
        if (minutes >= 3) {
            throw new IllegalStateException("编辑时间已过（3分钟内可编辑）");
        }

        review.setContent(req.getContent());
        review = reviewRepository.save(review);
        return toResponse(review, Collections.emptyMap(), Collections.emptySet(), new HashMap<>());
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + reviewId));

        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new SecurityException("无权删除他人书评");
        }

        reviewRepository.delete(review);
    }

    @Transactional
    public boolean toggleLike(Long reviewId, Long userId) {
        reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + reviewId));

        var existing = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);
        if (existing.isPresent()) {
            reviewLikeRepository.delete(existing.get());
            reviewRepository.decrementLikeCount(reviewId);
            return false;
        } else {
            reviewLikeRepository.save(new com.library.entity.ReviewLike(reviewId, userId));
            reviewRepository.incrementLikeCount(reviewId);
            return true;
        }
    }

    private ReviewResponse toResponse(Review review,
                                       Map<Long, List<Review>> repliesMap,
                                       Set<Long> likedIds,
                                       Map<Long, String> usernameCache) {
        String username = usernameCache.computeIfAbsent(review.getUserId(), uid ->
            userRepository.findById(uid).map(User::getUsername).orElse("未知用户"));

        List<ReviewResponse> replies = List.of();
        if (review.getParentId() == null) {
            List<Review> childReplies = repliesMap.getOrDefault(review.getId(), List.of());
            replies = childReplies.stream()
                .map(r -> toResponse(r, Collections.emptyMap(), likedIds, usernameCache))
                .collect(Collectors.toList());
        }

        return ReviewResponse.builder()
            .id(review.getId())
            .bookId(review.getBookId())
            .userId(review.getUserId())
            .username(username)
            .parentId(review.getParentId())
            .rootId(review.getRootId())
            .content(review.getContent())
            .likeCount(review.getLikeCount())
            .replyCount(review.getReplyCount())
            .liked(likedIds.contains(review.getId()))
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .replies(replies)
            .build();
    }
}
