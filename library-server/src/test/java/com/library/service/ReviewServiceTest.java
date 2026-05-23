package com.library.service;

import com.library.dto.ReviewRequest;
import com.library.dto.ReviewResponse;
import com.library.dto.PageResult;
import com.library.entity.Review;
import com.library.entity.User;
import com.library.repository.ReviewLikeRepository;
import com.library.repository.ReviewRepository;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock ReviewLikeRepository reviewLikeRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;
    @InjectMocks ReviewService reviewService;

    @Test
    void shouldCreateTopLevelReview() {
        ReviewRequest req = new ReviewRequest();
        req.setContent("好书！");
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.of(
            new User(1L, "小明", "pw", "READER", null)));

        ReviewResponse result = reviewService.createReview(10L, 1L, req);

        assertThat(result.getContent()).isEqualTo("好书！");
        assertThat(result.getUsername()).isEqualTo("小明");
        assertThat(result.getBookId()).isEqualTo(10L);
        assertThat(result.getParentId()).isNull();
    }

    @Test
    void shouldCreateReply() {
        Review topReview = Review.builder().id(5L).bookId(10L).userId(2L)
            .content("原评").likeCount(0).replyCount(0)
            .createdAt(LocalDateTime.now()).build();
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(topReview));

        ReviewRequest req = new ReviewRequest();
        req.setContent("回复内容");
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(6L);
            return r;
        });
        when(userRepository.findById(1L)).thenReturn(Optional.of(
            new User(1L, "回复者", "pw", "READER", null)));

        ReviewResponse result = reviewService.createReply(5L, 1L, req);

        assertThat(result.getContent()).isEqualTo("回复内容");
        assertThat(result.getParentId()).isEqualTo(5L);
        assertThat(result.getRootId()).isEqualTo(5L);
        verify(reviewRepository).incrementReplyCount(5L);
    }

    @Test
    void shouldGetReviewsSortedByTime() {
        Review review = Review.builder().id(1L).bookId(10L).userId(2L)
            .content("评").likeCount(3).replyCount(1).build();
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByBookIdAndParentIdIsNull(eq(10L), any(Pageable.class)))
            .thenReturn(page);
        when(reviewRepository.findByRootIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(userRepository.findById(2L)).thenReturn(Optional.of(
            new User(2L, "读者", "pw", "READER", null)));

        PageResult<ReviewResponse> result = reviewService.getReviews(10L, "time", 1, 10, null);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getUsername()).isEqualTo("读者");
    }

    @Test
    void shouldGetReviewsSortedByHot() {
        Review review = Review.builder().id(1L).bookId(10L).userId(2L)
            .content("评").likeCount(3).replyCount(1).build();
        Page<Review> page = new PageImpl<>(List.of(review));
        when(reviewRepository.findByBookIdAndParentIdIsNull(eq(10L), any(Pageable.class)))
            .thenReturn(page);
        when(reviewRepository.findByRootIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());
        when(userRepository.findById(2L)).thenReturn(Optional.of(
            new User(2L, "读者", "pw", "READER", null)));

        reviewService.getReviews(10L, "hot", 1, 10, null);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(reviewRepository).findByBookIdAndParentIdIsNull(eq(10L), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("likeCount")).isNotNull();
    }

    @Test
    void shouldThrowWhenEditingAfter3Minutes() {
        LocalDateTime old = LocalDateTime.now().minusMinutes(5);
        Review review = Review.builder().id(1L).userId(1L).content("旧")
            .createdAt(old).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewRequest req = new ReviewRequest();
        req.setContent("新内容");

        assertThatThrownBy(() -> reviewService.updateReview(1L, 1L, false, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("编辑时间已过");
    }

    @Test
    void shouldEditWithin3Minutes() {
        LocalDateTime recent = LocalDateTime.now().minusMinutes(1);
        Review review = Review.builder().id(1L).userId(1L).content("旧")
            .createdAt(recent).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(userRepository.findById(1L)).thenReturn(Optional.of(
            new User(1L, "小明", "pw", "READER", null)));

        ReviewRequest req = new ReviewRequest();
        req.setContent("新内容");

        ReviewResponse result = reviewService.updateReview(1L, 1L, false, req);

        assertThat(result.getContent()).isEqualTo("新内容");
    }

    @Test
    void shouldDeleteReview() {
        Review review = Review.builder().id(1L).userId(1L).parentId(null).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L, 1L, false);

        verify(reviewRepository).delete(review);
    }

    @Test
    void adminShouldDeleteAnyReview() {
        Review review = Review.builder().id(1L).userId(2L).parentId(null).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1L, 3L, true);

        verify(reviewRepository).delete(review);
    }

    @Test
    void shouldToggleLike() {
        Review review = Review.builder().id(1L).userId(10L).likeCount(5).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewIdAndUserId(1L, 10L))
            .thenReturn(Optional.empty());

        boolean liked = reviewService.toggleLike(1L, 10L);

        assertThat(liked).isTrue();
        verify(reviewLikeRepository).save(any());
        verify(reviewRepository).incrementLikeCount(1L);
    }

    @Test
    void shouldUntoggleLike() {
        Review review = Review.builder().id(1L).userId(10L).likeCount(5).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewIdAndUserId(1L, 10L))
            .thenReturn(Optional.of(new com.library.entity.ReviewLike(1L, 10L)));

        boolean liked = reviewService.toggleLike(1L, 10L);

        assertThat(liked).isFalse();
        verify(reviewLikeRepository).delete(any());
        verify(reviewRepository).decrementLikeCount(1L);
    }
}
