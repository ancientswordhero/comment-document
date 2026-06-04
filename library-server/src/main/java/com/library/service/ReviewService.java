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
    private final NotificationService notificationService;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewLikeRepository reviewLikeRepository,
                         UserRepository userRepository,
                         NotificationService notificationService) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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

        Map<Long, List<Review>> childrenMap = new HashMap<>();
        for (Long id : reviewIds) {
            List<Review> allReplies = reviewRepository.findByRootIdOrderByCreatedAtAsc(id);
            for (Review r : allReplies) {
                childrenMap.computeIfAbsent(r.getParentId(), k -> new ArrayList<>()).add(r);
            }
        }

        Set<Long> allReviewIds = new HashSet<>(reviewIds);
        for (List<Review> children : childrenMap.values()) {
            for (Review r : children) {
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
            .map(r -> toResponse(r, childrenMap, finalLikedIds, usernameCache))
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

        if (!parent.getUserId().equals(userId)) {
            String replierName = userRepository.findById(userId)
                .map(User::getUsername).orElse("未知用户");
            String snippet = req.getContent().length() > 30
                ? req.getContent().substring(0, 30) + "..." : req.getContent();
            notificationService.createNotification(parent.getUserId(), "reply",
                replierName + " 回复了你的书评",
                "回复内容：「" + snippet + "」",
                parent.getBookId(), parentId);
        }

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
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + reviewId));

        var existing = reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId);
        if (existing.isPresent()) {
            reviewLikeRepository.delete(existing.get());
            reviewRepository.decrementLikeCount(reviewId);
            return false;
        } else {
            reviewLikeRepository.save(new com.library.entity.ReviewLike(reviewId, userId));
            reviewRepository.incrementLikeCount(reviewId);

            if (!review.getUserId().equals(userId)) {
                String likerName = userRepository.findById(userId)
                    .map(User::getUsername).orElse("未知用户");
                String snippet = review.getContent().length() > 30
                    ? review.getContent().substring(0, 30) + "..." : review.getContent();
                notificationService.createNotification(review.getUserId(), "like",
                    likerName + " 赞了你的书评",
                    "书评内容：「" + snippet + "」",
                    review.getBookId(), reviewId);
            }

            return true;
        }
    }

    private ReviewResponse toResponse(Review review,
                                       Map<Long, List<Review>> childrenMap,
                                       Set<Long> likedIds,
                                       Map<Long, String> usernameCache) {
        String username = usernameCache.computeIfAbsent(review.getUserId(), uid ->
            userRepository.findById(uid).map(User::getUsername).orElse("未知用户"));

        List<ReviewResponse> replies = childrenMap.getOrDefault(review.getId(), List.of())
            .stream()
            .map(r -> toResponse(r, childrenMap, likedIds, usernameCache))
            .collect(Collectors.toList());

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
