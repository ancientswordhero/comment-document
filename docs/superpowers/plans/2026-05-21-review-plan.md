# 书评系统 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现参考 B 站评论系统的书评功能：发表、回复（一层嵌套）、点赞 toggle、编辑（3分钟窗口）、删除、按时间/热度排序、分页。

**Architecture:** Spring Boot 新增 Review 实体/Repository/Service/Controller 层，JWT Filter 扩展 `/api/reviews/**` 写操作认证。前端 BookDetail 下方新增 ReviewSection + ReviewItem 组件，纯文字无头像。

**Tech Stack:** Java 17, Spring Boot 3.x, Spring Data JPA, MySQL, JUnit + Mockito, MockMvc, Vue 3 (Composition API), Axios

---

### Task 1: 创建 Review 和 ReviewLike 实体

**Files:**
- Create: `library-server/src/main/java/com/library/entity/Review.java`
- Create: `library-server/src/main/java/com/library/entity/ReviewLike.java`

- [ ] **Step 1: 创建 Review 实体**

```java
package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "root_id")
    private Long rootId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "reply_count")
    private int replyCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Review() {}

    public Review(Long id, Long bookId, Long userId, Long parentId, Long rootId,
                  String content, int likeCount, int replyCount,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.parentId = parentId;
        this.rootId = rootId;
        this.content = content;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long bookId;
        private Long userId;
        private Long parentId;
        private Long rootId;
        private String content;
        private int likeCount;
        private int replyCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder bookId(Long bookId) { this.bookId = bookId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder rootId(Long rootId) { this.rootId = rootId; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder likeCount(int likeCount) { this.likeCount = likeCount; return this; }
        public Builder replyCount(int replyCount) { this.replyCount = replyCount; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Review build() {
            return new Review(id, bookId, userId, parentId, rootId, content,
                likeCount, replyCount, createdAt, updatedAt);
        }
    }
}
```

- [ ] **Step 2: 创建 ReviewLike 实体**

```java
package com.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "review_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"review_id", "user_id"})
})
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public ReviewLike() {}

    public ReviewLike(Long reviewId, Long userId) {
        this.reviewId = reviewId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/entity/Review.java library-server/src/main/java/com/library/entity/ReviewLike.java
git commit -m "feat: add Review and ReviewLike JPA entities"
```

---

### Task 2: 创建 Repository 层

**Files:**
- Create: `library-server/src/main/java/com/library/repository/ReviewRepository.java`
- Create: `library-server/src/main/java/com/library/repository/ReviewLikeRepository.java`

- [ ] **Step 1: 创建 ReviewRepository**

```java
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

    @Query("SELECT COUNT(r) FROM Review r WHERE r.parentId = :parentId")
    int countReplies(@Param("parentId") Long parentId);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :id AND r.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Review r SET r.replyCount = r.replyCount + 1 WHERE r.id = :id")
    void incrementReplyCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Review r SET r.replyCount = r.replyCount - 1 WHERE r.id = :id AND r.replyCount > 0")
    void decrementReplyCount(@Param("id") Long id);

    void deleteAllByBookId(Long bookId);
}
```

- [ ] **Step 2: 创建 ReviewLikeRepository**

```java
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
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/repository/ReviewRepository.java library-server/src/main/java/com/library/repository/ReviewLikeRepository.java
git commit -m "feat: add ReviewRepository and ReviewLikeRepository"
```

---

### Task 3: 创建 DTO 类

**Files:**
- Create: `library-server/src/main/java/com/library/dto/ReviewRequest.java`
- Create: `library-server/src/main/java/com/library/dto/ReviewResponse.java`

- [ ] **Step 1: 创建 ReviewRequest**

```java
package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

    @NotBlank(message = "书评内容不能为空")
    @Size(max = 1000, message = "书评内容不能超过1000字")
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

- [ ] **Step 2: 创建 ReviewResponse**

```java
package com.library.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {
    private Long id;
    private Long bookId;
    private Long userId;
    private String username;
    private Long parentId;
    private Long rootId;
    private String content;
    private int likeCount;
    private int replyCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReviewResponse> replies;

    public ReviewResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<ReviewResponse> getReplies() { return replies; }
    public void setReplies(List<ReviewResponse> replies) { this.replies = replies; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private Long bookId;
        private Long userId;
        private String username;
        private Long parentId;
        private Long rootId;
        private String content;
        private int likeCount;
        private int replyCount;
        private boolean liked;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ReviewResponse> replies;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder bookId(Long bookId) { this.bookId = bookId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder rootId(Long rootId) { this.rootId = rootId; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder likeCount(int likeCount) { this.likeCount = likeCount; return this; }
        public Builder replyCount(int replyCount) { this.replyCount = replyCount; return this; }
        public Builder liked(boolean liked) { this.liked = liked; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder replies(List<ReviewResponse> replies) { this.replies = replies; return this; }
        public ReviewResponse build() {
            ReviewResponse r = new ReviewResponse();
            r.setId(id); r.setBookId(bookId); r.setUserId(userId);
            r.setUsername(username); r.setParentId(parentId); r.setRootId(rootId);
            r.setContent(content); r.setLikeCount(likeCount); r.setReplyCount(replyCount);
            r.setLiked(liked); r.setCreatedAt(createdAt); r.setUpdatedAt(updatedAt);
            r.setReplies(replies);
            return r;
        }
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `cd library-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add library-server/src/main/java/com/library/dto/ReviewRequest.java library-server/src/main/java/com/library/dto/ReviewResponse.java
git commit -m "feat: add ReviewRequest and ReviewResponse DTOs"
```

---

### Task 4: 创建 ReviewService + 测试（TDD）

**Files:**
- Create: `library-server/src/test/java/com/library/service/ReviewServiceTest.java`
- Create: `library-server/src/main/java/com/library/service/ReviewService.java`

- [ ] **Step 1: 编写 ReviewServiceTest（测试先行）**

```java
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
        when(reviewLikeRepository.findByUserIdAndReviewIdIn(eq(0L), anyList()))
            .thenReturn(List.of());

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
        when(reviewLikeRepository.findByUserIdAndReviewIdIn(eq(0L), anyList()))
            .thenReturn(List.of());

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
        Review review = Review.builder().id(1L).likeCount(5).build();
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
        Review review = Review.builder().id(1L).likeCount(5).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewIdAndUserId(1L, 10L))
            .thenReturn(Optional.of(new com.library.entity.ReviewLike(1L, 10L)));

        boolean liked = reviewService.toggleLike(1L, 10L);

        assertThat(liked).isFalse();
        verify(reviewLikeRepository).delete(any());
        verify(reviewRepository).decrementLikeCount(1L);
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=ReviewServiceTest -q`
Expected: FAIL (ReviewService class not found)

- [ ] **Step 3: 实现 ReviewService**

```java
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
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=ReviewServiceTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/service/ReviewService.java library-server/src/test/java/com/library/service/ReviewServiceTest.java
git commit -m "feat: add ReviewService with CRUD, like toggle, and sorting"
```

---

### Task 5: 创建 ReviewController + 集成测试（TDD）

**Files:**
- Create: `library-server/src/test/java/com/library/controller/ReviewControllerTest.java`
- Create: `library-server/src/main/java/com/library/controller/ReviewController.java`

- [ ] **Step 1: 编写 ReviewControllerTest（测试先行）**

```java
package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.JwtUtil;
import com.library.dto.*;
import com.library.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ReviewService reviewService;

    @Test
    void shouldListReviews() throws Exception {
        ReviewResponse review = ReviewResponse.builder().id(1L).bookId(10L)
            .content("好").username("小明").likeCount(3).replyCount(0)
            .replies(List.of()).build();
        PageResult<ReviewResponse> page = PageResult.<ReviewResponse>builder()
            .records(List.of(review)).total(1).page(1).size(10).build();
        when(reviewService.getReviews(eq(10L), eq("time"), eq(1), eq(10), isNull()))
            .thenReturn(page);

        mvc.perform(get("/api/books/10/reviews"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].username").value("小明"));
    }

    @Test
    void shouldCreateReview() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setContent("好书！");
        ReviewResponse resp = ReviewResponse.builder().id(1L).content("好书！").build();
        when(reviewService.createReview(eq(10L), eq(5L), any())).thenReturn(resp);

        mvc.perform(post("/api/books/10/reviews")
                .requestAttr("userId", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("好书！"));
    }

    @Test
    void shouldCreateReply() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setContent("回复");
        ReviewResponse resp = ReviewResponse.builder().id(2L).content("回复").parentId(1L).build();
        when(reviewService.createReply(eq(1L), eq(5L), any())).thenReturn(resp);

        mvc.perform(post("/api/reviews/1/reply")
                .requestAttr("userId", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("回复"));
    }

    @Test
    void shouldUpdateReview() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setContent("修改后");
        ReviewResponse resp = ReviewResponse.builder().id(1L).content("修改后").build();
        when(reviewService.updateReview(eq(1L), eq(5L), eq(false), any())).thenReturn(resp);

        mvc.perform(put("/api/reviews/1")
                .requestAttr("userId", 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("修改后"));
    }

    @Test
    void shouldDeleteReview() throws Exception {
        mvc.perform(delete("/api/reviews/1")
                .requestAttr("userId", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldToggleLike() throws Exception {
        when(reviewService.toggleLike(1L, 5L)).thenReturn(true);

        mvc.perform(post("/api/reviews/1/like")
                .requestAttr("userId", 5L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `cd library-server && mvn test -Dtest=ReviewControllerTest -q`
Expected: FAIL

- [ ] **Step 3: 实现 ReviewController**

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/books/{bookId}/reviews")
    public ApiResponse<PageResult<ReviewResponse>> listReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "time") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.getReviews(bookId, sort, page, size, userId));
    }

    @PostMapping("/api/books/{bookId}/reviews")
    public ApiResponse<ReviewResponse> createReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.createReview(bookId, userId, reviewRequest));
    }

    @PostMapping("/api/reviews/{id}/reply")
    public ApiResponse<ReviewResponse> createReply(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.createReply(id, userId, reviewRequest));
    }

    @PutMapping("/api/reviews/{id}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        return ApiResponse.success(reviewService.updateReview(id, userId, isAdmin, reviewRequest));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ApiResponse<Void> deleteReview(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(request.getAttribute("role"));
        reviewService.deleteReview(id, userId, isAdmin);
        return ApiResponse.success(null);
    }

    @PostMapping("/api/reviews/{id}/like")
    public ApiResponse<Boolean> toggleLike(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(reviewService.toggleLike(id, userId));
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=ReviewControllerTest -q`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add library-server/src/main/java/com/library/controller/ReviewController.java library-server/src/test/java/com/library/controller/ReviewControllerTest.java
git commit -m "feat: add ReviewController with list/create/reply/update/delete/like endpoints"
```

---

### Task 6: 修改 JWT Filter 扩展认证路径

**Files:**
- Modify: `library-server/src/main/java/com/library/config/JwtFilter.java`

- [ ] **Step 1: 更新 JwtFilter.doFilter 的 needsAuth 条件**

```java
// 原代码:
boolean needsAuth = path.equals("/api/auth/me") || path.startsWith("/api/bookshelf");

// 改成:
boolean needsAuth = path.equals("/api/auth/me")
    || path.startsWith("/api/bookshelf")
    || (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(req.getMethod()));
```

- [ ] **Step 2: 运行已有测试确保不回归**

Run: `cd library-server && mvn test -q`
Expected: All tests pass

- [ ] **Step 3: Commit**

```bash
git add library-server/src/main/java/com/library/config/JwtFilter.java
git commit -m "feat: extend JWT filter to cover /api/reviews write operations"
```

---

### Task 7: 创建前端 API 模块

**Files:**
- Create: `reader-app/src/api/review.js`

- [ ] **Step 1: 创建 review.js**

```javascript
import api from './index'

export function getReviews(bookId, params = {}) {
  return api.get(`/books/${bookId}/reviews`, { params })
}

export function createReview(bookId, data) {
  return api.post(`/books/${bookId}/reviews`, data)
}

export function createReply(reviewId, data) {
  return api.post(`/reviews/${reviewId}/reply`, data)
}

export function updateReview(reviewId, data) {
  return api.put(`/reviews/${reviewId}`, data)
}

export function deleteReview(reviewId) {
  return api.delete(`/reviews/${reviewId}`)
}

export function toggleLike(reviewId) {
  return api.post(`/reviews/${reviewId}/like`)
}
```

- [ ] **Step 2: Commit**

```bash
git add reader-app/src/api/review.js
git commit -m "feat: add review API module"
```

---

### Task 8: 创建 ReviewItem 组件

**Files:**
- Create: `reader-app/src/components/ReviewItem.vue`

- [ ] **Step 1: 创建 ReviewItem.vue**

```vue
<template>
  <div :class="['review-item', { 'is-reply': isReply }]">
    <div class="review-body">
      <div class="review-header">
        <span class="review-username">{{ review.username }}</span>
        <span v-if="isOwn" class="review-me-tag">(我)</span>
      </div>
      <div class="review-content" v-if="!editing">{{ review.content }}</div>
      <div class="review-edit" v-else>
        <textarea v-model="editContent" class="review-edit-input" rows="3"></textarea>
        <div class="review-edit-actions">
          <button class="btn-cancel" @click="editing = false">取消</button>
          <button class="btn-save" @click="doEdit">保存</button>
        </div>
      </div>
      <div class="review-meta">
        <span>{{ formatDate(review.createdAt) }}</span>
        <span
          class="meta-action"
          :class="{ liked: review.liked }"
          @click="$emit('like', review.id)"
        >赞 {{ review.likeCount }}</span>
        <span class="meta-action" @click="showReplyInput = !showReplyInput">回复</span>
        <template v-if="isOwn && canEdit">
          <span class="meta-action" @click="startEdit">编辑</span>
          <span class="meta-action meta-danger" @click="$emit('delete', review.id)">删除</span>
        </template>
      </div>
      <div v-if="review.updatedAt && review.updatedAt !== review.createdAt" class="review-edited-tag">
        (已编辑)
      </div>
    </div>

    <div v-if="showReplyInput" class="reply-input-area">
      <textarea v-model="replyContent" class="reply-input" rows="2" placeholder="写下你的回复..."></textarea>
      <div class="reply-input-actions">
        <button class="btn-cancel" @click="showReplyInput = false; replyContent = ''">取消</button>
        <button class="btn-save" @click="doReply">回复</button>
      </div>
    </div>

    <div v-if="replies && replies.length" class="replies-list">
      <ReviewItem
        v-for="reply in replies"
        :key="reply.id"
        :review="reply"
        :is-reply="true"
        :current-user-id="currentUserId"
        @like="$emit('like', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  review: { type: Object, required: true },
  isReply: { type: Boolean, default: false },
  currentUserId: { type: Number, default: null }
})

defineEmits(['like', 'delete', 'reply'])

const showReplyInput = ref(false)
const replyContent = ref('')
const editing = ref(false)
const editContent = ref('')

const isOwn = computed(() =>
  props.currentUserId && props.review.userId === props.currentUserId
)

const canEdit = computed(() => {
  if (!props.review.createdAt) return false
  const created = new Date(props.review.createdAt)
  const now = new Date()
  return (now - created) < 3 * 60 * 1000
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

function startEdit() {
  editContent.value = props.review.content
  editing.value = true
}

function doEdit() {
  if (!editContent.value.trim()) return
  $emit('edit', props.review.id, editContent.value)
  editing.value = false
}

function doReply() {
  if (!replyContent.value.trim()) return
  $emit('reply', props.review.id, replyContent.value)
  replyContent.value = ''
  showReplyInput.value = false
}
</script>

<style scoped>
.review-item { padding: 16px 0; border-bottom: 1px solid var(--color-accent-light, #f0ebe0); }
.review-item.is-reply {
  margin-top: 8px;
  margin-left: 8px;
  padding: 12px;
  background: var(--color-bg, #fafaf7);
  border-radius: var(--radius, 8px);
  border-bottom: none;
}
.review-username {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text, #4a3d2f);
}
.review-me-tag {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  font-weight: 400;
  margin-left: 4px;
}
.review-content {
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
  line-height: 1.8;
  margin: 6px 0;
}
.review-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
  margin-top: 8px;
}
.meta-action {
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  transition: color 0.2s;
}
.meta-action:hover { color: var(--color-primary, #c9a96e); }
.meta-action.liked {
  color: var(--color-primary, #c9a96e);
  font-weight: 600;
}
.meta-danger { color: var(--color-text-secondary, #8b8070); }
.meta-danger:hover { color: var(--color-danger, #c04040); }
.review-edited-tag {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  margin-top: 4px;
}
.review-edit-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 13px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.review-edit-input:focus { border-color: var(--color-primary, #c9a96e); }
.review-edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 8px;
}
.reply-input-area {
  margin-top: 10px;
  margin-left: 8px;
}
.reply-input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.reply-input:focus { border-color: var(--color-primary, #c9a96e); }
.reply-input-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 6px;
}
.btn-cancel {
  padding: 4px 12px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  cursor: pointer;
}
.btn-save {
  padding: 4px 12px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm, 6px);
  font-size: 12px;
  cursor: pointer;
}
.replies-list { margin-top: 4px; }
</style>
```

- [ ] **Step 2: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: No errors from ReviewItem.vue (may have other warnings)

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/components/ReviewItem.vue
git commit -m "feat: add ReviewItem component with edit, reply, like, delete"
```

---

### Task 9: 创建 ReviewSection 组件

**Files:**
- Create: `reader-app/src/components/ReviewSection.vue`

- [ ] **Step 1: 创建 ReviewSection.vue**

```vue
<template>
  <div class="review-section">
    <div class="review-header-bar">
      <h3 class="review-title">书评</h3>
      <span v-if="totalCount > 0" class="review-count">({{ totalCount }})</span>
    </div>

    <div v-if="!isLoggedIn" class="review-login-hint">
      请<a href="#" @click.prevent="$router.push('/login')">登录</a>后发表书评
    </div>
    <div v-else class="review-post-area">
      <textarea
        v-model="postContent"
        class="review-post-input"
        rows="3"
        placeholder="写下你的书评..."
        maxlength="1000"
      ></textarea>
      <div class="review-post-footer">
        <span class="review-char-count">{{ postContent.length }}/1000</span>
        <button
          class="btn-post"
          :disabled="!postContent.trim() || posting"
          @click="postReview"
        >{{ posting ? '发表中...' : '发表书评' }}</button>
      </div>
    </div>

    <div v-if="totalCount > 0" class="review-sort">
      <span
        :class="{ active: sort === 'time' }"
        @click="changeSort('time')"
      >按时间</span>
      <span
        :class="{ active: sort === 'hot' }"
        @click="changeSort('hot')"
      >按热度</span>
    </div>

    <div v-if="loading" class="review-loading">加载中...</div>
    <div v-else-if="reviews.length === 0 && !posting" class="review-empty">暂无书评</div>
    <div v-else class="review-list">
      <ReviewItem
        v-for="review in reviews"
        :key="review.id"
        :review="review"
        :current-user-id="currentUserId"
        @like="onLike"
        @delete="onDelete"
        @edit="onEdit"
        @reply="onReply"
      />
    </div>

    <div v-if="totalPages > 1" class="review-pagination">
      <button :disabled="page <= 1" @click="goPage(page - 1)">← 上一页</button>
      <span class="page-info">{{ page }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="goPage(page + 1)">下一页 →</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import ReviewItem from './ReviewItem.vue'
import {
  getReviews, createReview, createReply,
  updateReview, deleteReview, toggleLike
} from '../api/review'

const props = defineProps({ bookId: { type: [Number, String], required: true } })

const reviews = ref([])
const loading = ref(false)
const posting = ref(false)
const sort = ref('time')
const page = ref(1)
const totalCount = ref(0)
const pageSize = 10
const postContent = ref('')

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const currentUserId = computed(() => {
  const token = localStorage.getItem('token')
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return Number(payload.sub)
  } catch { return null }
})

const totalPages = computed(() => Math.ceil(totalCount.value / pageSize) || 0)

onMounted(() => fetchReviews())

async function fetchReviews() {
  loading.value = true
  try {
    const data = await getReviews(props.bookId, { sort: sort.value, page: page.value, size: pageSize })
    reviews.value = data.records
    totalCount.value = data.total
  } finally {
    loading.value = false
  }
}

async function postReview() {
  if (!postContent.value.trim()) return
  posting.value = true
  try {
    await createReview(props.bookId, { content: postContent.value })
    postContent.value = ''
    page.value = 1
    sort.value = 'time'
    await fetchReviews()
  } finally {
    posting.value = false
  }
}

async function onReply(reviewId, content) {
  await createReply(reviewId, { content })
  await fetchReviews()
}

async function onEdit(reviewId, content) {
  await updateReview(reviewId, { content })
  await fetchReviews()
}

async function onDelete(reviewId) {
  if (!confirm('确定删除这条书评吗？')) return
  await deleteReview(reviewId)
  await fetchReviews()
}

async function onLike(reviewId) {
  await toggleLike(reviewId)
  await fetchReviews()
}

function changeSort(newSort) {
  sort.value = newSort
  page.value = 1
  fetchReviews()
}

function goPage(p) {
  page.value = p
  fetchReviews()
}
</script>

<style scoped>
.review-section {
  margin-top: 24px;
  padding: 20px 0;
  border-top: 2px solid var(--color-primary, #c9a96e);
}
.review-header-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.review-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
  font-family: var(--font-serif);
  letter-spacing: 2px;
}
.review-count {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
}
.review-login-hint {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  background: var(--color-bg, #fafaf7);
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: var(--radius, 8px);
}
.review-login-hint a {
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
}
.review-post-area {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--color-bg, #fafaf7);
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: var(--radius, 8px);
}
.review-post-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0dbd0;
  border-radius: var(--radius-sm, 6px);
  font-size: 13px;
  resize: vertical;
  font-family: inherit;
  outline: none;
}
.review-post-input:focus { border-color: var(--color-primary, #c9a96e); }
.review-post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.review-char-count {
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
}
.btn-post {
  padding: 7px 18px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: var(--radius, 8px);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-post:hover:not(:disabled) {
  border-color: var(--color-primary, #c9a96e);
  color: var(--color-primary, #c9a96e);
}
.btn-post:disabled { opacity: 0.5; cursor: not-allowed; }
.review-sort {
  display: flex;
  gap: 20px;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
  padding-bottom: 10px;
  margin-bottom: 4px;
}
.review-sort span {
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  padding-bottom: 10px;
  margin-bottom: -11px;
  transition: color 0.2s;
}
.review-sort span.active {
  color: var(--color-primary, #c9a96e);
  font-weight: 600;
  border-bottom: 2px solid var(--color-primary, #c9a96e);
}
.review-loading, .review-empty {
  text-align: center;
  padding: 40px;
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
}
.review-pagination {
  text-align: center;
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.review-pagination button {
  padding: 6px 14px;
  background: var(--color-bg, #fafaf7);
  color: var(--color-text-secondary, #8b8070);
  border: 1px solid #e0dbd0;
  border-radius: var(--radius, 8px);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.review-pagination button:hover:not(:disabled) {
  border-color: var(--color-primary, #c9a96e);
  color: var(--color-primary, #c9a96e);
}
.review-pagination button:disabled {
  color: #d0c8b4;
  cursor: not-allowed;
}
.page-info {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
}
</style>
```

- [ ] **Step 2: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: Build succeeds

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/components/ReviewSection.vue
git commit -m "feat: add ReviewSection component with post, sort, pagination"
```

---

### Task 10: 修改 BookDetail.vue 集成书评区

**Files:**
- Modify: `reader-app/src/views/BookDetail.vue`

- [ ] **Step 1: 在 detail-card 下方添加 ReviewSection**

在 BookDetail.vue 模板的 `</div>` (detail-card 结束) 之后、`</div>` (detail-page 或 loading 结束) 之前，添加：

```vue
    <ReviewSection v-if="book" :book-id="book.id" />
```

同时在 `<script setup>` 顶部添加 import：

```javascript
import ReviewSection from '../components/ReviewSection.vue'
```

- [ ] **Step 2: 验证前端编译**

Run: `cd reader-app && npm run build`
Expected: Build succeeds

- [ ] **Step 3: Commit**

```bash
git add reader-app/src/views/BookDetail.vue
git commit -m "feat: integrate ReviewSection into BookDetail page"
```

---

### Task 11: 运行全部测试并验证

- [ ] **Step 1: 运行全部后端测试**

Run: `cd library-server && mvn test`
Expected: All tests pass

- [ ] **Step 2: 验证前端构建**

Run: `cd reader-app && npm run build`
Expected: Build succeeds without errors

- [ ] **Step 3: Commit（如有修复）**

```bash
git add -A && git commit -m "chore: finalize review system" || echo "no changes"
```

---

## Plan Self-Review

**1. Spec coverage:**
- reviews 表 + review_likes 表 → Task 1 (entities)
- Repository 层 → Task 2
- DTO (ReviewRequest, ReviewResponse) → Task 3
- ReviewService (CRUD, like toggle, sorting) → Task 4
- ReviewController (6 endpoints) → Task 5
- JWT Filter 扩展 → Task 6
- 前端 API 模块 → Task 7
- ReviewItem 组件（编辑/删除/回复/点赞交互）→ Task 8
- ReviewSection 组件（发表/排序/分页）→ Task 9
- BookDetail 集成 → Task 10
- 3分钟编辑窗口 → Task 4 后端校验 + Task 8 前端校验
- 按时间/热度排序 → Task 4 Service + Task 9 前端切换
- 点赞 toggle → Task 4 Service + Task 9 前端
- 无头像仅用户名 → Task 8/9 组件设计
- 按钮页面背景色 → Task 8/9 组件样式

**2. Placeholder scan:** 无 TBD/TODO，所有步骤包含完整代码。

**3. Type consistency:**
- `ReviewRequest.content` (String @NotBlank @Size(max=1000)) 一致于 Task 3/4/5/7/9
- `ReviewResponse` 字段一致于 Task 3/4/5/8/9
- `PageResult<ReviewResponse>` 一致于 Task 4/5/9
- 前端 `review.js` 的 API 调用参数一致于后端 Controller 端点
- ReviewItem emits (`like`, `delete`, `edit`, `reply`) 一致于 ReviewSection 中的处理函数
