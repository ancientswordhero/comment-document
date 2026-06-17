# 书余 & 凡例 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现书余（阅读批注 + 思余/余音双Tab）和凡例（静态帮助页）

**Architecture:** 后端新增 Note/NoteLike 实体及配套 Service/Controller，扩展现有 Report 系统支持 note 类型举报。前端新增 Notes.vue（思余+余音）、Guide.vue（凡例），改造 BookReader.vue 增加段落级批注 UI。

**Tech Stack:** Spring Boot 3 + JPA + MySQL (后端), Vue 3 + epubjs + Axios (前端)

---

### Task 1: Note 实体

**Files:**
- Create: `library-server/src/main/java/com/library/entity/Note.java`
- Test: `library-server/src/test/java/com/library/entity/NoteTest.java`

- [ ] **Step 1: 编写 Note 实体**

```java
package com.library.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "selected_text", length = 500)
    private String selectedText;

    @Column(length = 500)
    private String cfi;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "is_published")
    private boolean published;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "root_id")
    private Long rootId;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "reply_count")
    private int replyCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Note() {}

    public Note(Long id, Long userId, Long bookId, String content, String selectedText,
                String cfi, String type, boolean published, Long parentId, Long rootId,
                int likeCount, int replyCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.content = content;
        this.selectedText = selectedText;
        this.cfi = cfi;
        this.type = type;
        this.published = published;
        this.parentId = parentId;
        this.rootId = rootId;
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
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
    public String getCfi() { return cfi; }
    public void setCfi(String cfi) { this.cfi = cfi; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
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
        private Long id, userId, bookId, parentId, rootId;
        private String content, selectedText, cfi, type;
        private boolean published;
        private int likeCount, replyCount;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(Long v) { id = v; return this; }
        public Builder userId(Long v) { userId = v; return this; }
        public Builder bookId(Long v) { bookId = v; return this; }
        public Builder content(String v) { content = v; return this; }
        public Builder selectedText(String v) { selectedText = v; return this; }
        public Builder cfi(String v) { cfi = v; return this; }
        public Builder type(String v) { type = v; return this; }
        public Builder published(boolean v) { published = v; return this; }
        public Builder parentId(Long v) { parentId = v; return this; }
        public Builder rootId(Long v) { rootId = v; return this; }
        public Builder likeCount(int v) { likeCount = v; return this; }
        public Builder replyCount(int v) { replyCount = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { updatedAt = v; return this; }
        public Note build() {
            return new Note(id, userId, bookId, content, selectedText, cfi, type,
                published, parentId, rootId, likeCount, replyCount, createdAt, updatedAt);
        }
    }
}
```

- [ ] **Step 2: 编写 Note 实体基本测试**

```java
package com.library.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NoteTest {

    @Test
    void shouldCreateNoteWithBuilder() {
        Note note = Note.builder()
            .userId(1L).bookId(10L)
            .content("这段意境真美")
            .selectedText("落霞与孤鹜齐飞")
            .cfi("epubcfi(/6/4!/4/2/2)")
            .type("INSIGHT")
            .published(false)
            .likeCount(0).replyCount(0)
            .build();

        assertThat(note.getUserId()).isEqualTo(1L);
        assertThat(note.getBookId()).isEqualTo(10L);
        assertThat(note.getContent()).isEqualTo("这段意境真美");
        assertThat(note.getSelectedText()).isEqualTo("落霞与孤鹜齐飞");
        assertThat(note.getCfi()).isEqualTo("epubcfi(/6/4!/4/2/2)");
        assertThat(note.getType()).isEqualTo("INSIGHT");
        assertThat(note.isPublished()).isFalse();
    }

    @Test
    void shouldSetTimestampsOnPersist() {
        Note note = new Note();
        note.onCreate();
        assertThat(note.getCreatedAt()).isNotNull();
        assertThat(note.getUpdatedAt()).isNotNull();
    }
}
```

- [ ] **Step 3: 运行测试验证通过**

Run: `cd library-server && mvn test -Dtest=NoteTest -pl .`
Expected: PASS (2 tests)

- [ ] **Step 4: 提交**

```bash
git add library-server/src/main/java/com/library/entity/Note.java \
        library-server/src/test/java/com/library/entity/NoteTest.java
git commit -m "feat: add Note entity for reading annotations"
```

---

### Task 2: NoteLike 实体

**Files:**
- Create: `library-server/src/main/java/com/library/entity/NoteLike.java`

- [ ] **Step 1: 编写 NoteLike 实体**

```java
package com.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "note_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"note_id", "user_id"})
})
public class NoteLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public NoteLike() {}

    public NoteLike(Long noteId, Long userId) {
        this.noteId = noteId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
```

- [ ] **Step 2: 提交**

```bash
git add library-server/src/main/java/com/library/entity/NoteLike.java
git commit -m "feat: add NoteLike entity for note likes"
```

---

### Task 3: NoteRepository + NoteLikeRepository

**Files:**
- Create: `library-server/src/main/java/com/library/repository/NoteRepository.java`
- Create: `library-server/src/main/java/com/library/repository/NoteLikeRepository.java`

- [ ] **Step 1: 编写 NoteRepository**

```java
package com.library.repository;

import com.library.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // 思余：用户的私密手记（非回复）
    Page<Note> findByUserIdAndParentIdIsNullAndPublishedFalseOrderByCreatedAtDesc(
        Long userId, Pageable pageable);

    // 某本书下用户的思余
    List<Note> findByUserIdAndBookIdAndParentIdIsNullAndPublishedFalseOrderByCreatedAtDesc(
        Long userId, Long bookId);

    // 按类型筛选思余
    Page<Note> findByUserIdAndParentIdIsNullAndPublishedFalseAndTypeOrderByCreatedAtDesc(
        Long userId, String type, Pageable pageable);

    // 用户已发布的手记（非回复）
    Page<Note> findByUserIdAndParentIdIsNullAndPublishedTrueOrderByCreatedAtDesc(
        Long userId, Pageable pageable);

    // 余音广场：全站公开手记（非回复）
    Page<Note> findByParentIdIsNullAndPublishedTrue(Pageable pageable);

    // 余音广场中属于指定书籍ID列表的手记
    Page<Note> findByParentIdIsNullAndPublishedTrueAndBookIdIn(
        List<Long> bookIds, Pageable pageable);

    // 余音广场中不属于指定书籍ID列表的手记
    Page<Note> findByParentIdIsNullAndPublishedTrueAndBookIdNotIn(
        List<Long> bookIds, Pageable pageable);

    // 某本书的公开手记
    Page<Note> findByBookIdAndParentIdIsNullAndPublishedTrueOrderByCreatedAtDesc(
        Long bookId, Pageable pageable);

    // 回复查询（树形）
    List<Note> findByRootIdOrderByCreatedAtAsc(Long rootId);

    @Modifying
    @Query("UPDATE Note n SET n.likeCount = n.likeCount + 1 WHERE n.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Note n SET n.likeCount = n.likeCount - 1 WHERE n.id = :id AND n.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Note n SET n.replyCount = n.replyCount + 1 WHERE n.id = :id")
    void incrementReplyCount(@Param("id") Long id);
}
```

- [ ] **Step 2: 编写 NoteLikeRepository**

```java
package com.library.repository;

import com.library.entity.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteLikeRepository extends JpaRepository<NoteLike, Long> {

    Optional<NoteLike> findByNoteIdAndUserId(Long noteId, Long userId);

    List<NoteLike> findByUserIdAndNoteIdIn(Long userId, List<Long> noteIds);
}
```

- [ ] **Step 3: 提交**

```bash
git add library-server/src/main/java/com/library/repository/NoteRepository.java \
        library-server/src/main/java/com/library/repository/NoteLikeRepository.java
git commit -m "feat: add NoteRepository and NoteLikeRepository"
```

---

### Task 4: NoteRequest / NoteResponse DTO

**Files:**
- Create: `library-server/src/main/java/com/library/dto/NoteRequest.java`
- Create: `library-server/src/main/java/com/library/dto/NoteResponse.java`

- [ ] **Step 1: 编写 NoteRequest**

```java
package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NoteRequest {

    @NotBlank(message = "内容不能为空")
    private String content;

    @Size(max = 500, message = "引用原文不能超过500字")
    private String selectedText;

    @Size(max = 500, message = "CFI不能超过500字")
    private String cfi;

    @NotBlank(message = "类型不能为空")
    private String type;

    private boolean publish;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
    public String getCfi() { return cfi; }
    public void setCfi(String cfi) { this.cfi = cfi; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isPublish() { return publish; }
    public void setPublish(boolean publish) { this.publish = publish; }
}
```

- [ ] **Step 2: 编写 NoteResponse**

```java
package com.library.dto;

import java.time.LocalDateTime;
import java.util.List;

public class NoteResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private String content;
    private String selectedText;
    private String cfi;
    private String type;
    private boolean published;
    private Long parentId;
    private Long rootId;
    private int likeCount;
    private int replyCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NoteResponse> replies;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
    public String getCfi() { return cfi; }
    public void setCfi(String cfi) { this.cfi = cfi; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
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
    public List<NoteResponse> getReplies() { return replies; }
    public void setReplies(List<NoteResponse> replies) { this.replies = replies; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, userId, bookId, parentId, rootId;
        private String username, bookTitle, content, selectedText, cfi, type;
        private boolean published, liked;
        private int likeCount, replyCount;
        private LocalDateTime createdAt, updatedAt;
        private List<NoteResponse> replies;

        public Builder id(Long v) { id = v; return this; }
        public Builder userId(Long v) { userId = v; return this; }
        public Builder username(String v) { username = v; return this; }
        public Builder bookId(Long v) { bookId = v; return this; }
        public Builder bookTitle(String v) { bookTitle = v; return this; }
        public Builder content(String v) { content = v; return this; }
        public Builder selectedText(String v) { selectedText = v; return this; }
        public Builder cfi(String v) { cfi = v; return this; }
        public Builder type(String v) { type = v; return this; }
        public Builder published(boolean v) { published = v; return this; }
        public Builder parentId(Long v) { parentId = v; return this; }
        public Builder rootId(Long v) { rootId = v; return this; }
        public Builder likeCount(int v) { likeCount = v; return this; }
        public Builder replyCount(int v) { replyCount = v; return this; }
        public Builder liked(boolean v) { liked = v; return this; }
        public Builder createdAt(LocalDateTime v) { createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { updatedAt = v; return this; }
        public Builder replies(List<NoteResponse> v) { replies = v; return this; }
        public NoteResponse build() {
            NoteResponse r = new NoteResponse();
            r.setId(id); r.setUserId(userId); r.setUsername(username);
            r.setBookId(bookId); r.setBookTitle(bookTitle);
            r.setContent(content); r.setSelectedText(selectedText); r.setCfi(cfi);
            r.setType(type); r.setPublished(published);
            r.setParentId(parentId); r.setRootId(rootId);
            r.setLikeCount(likeCount); r.setReplyCount(replyCount);
            r.setLiked(liked); r.setCreatedAt(createdAt); r.setUpdatedAt(updatedAt);
            r.setReplies(replies);
            return r;
        }
    }
}
```

- [ ] **Step 3: 提交**

```bash
git add library-server/src/main/java/com/library/dto/NoteRequest.java \
        library-server/src/main/java/com/library/dto/NoteResponse.java
git commit -m "feat: add NoteRequest and NoteResponse DTOs"
```

---

### Task 5: NoteService

**Files:**
- Create: `library-server/src/main/java/com/library/service/NoteService.java`
- Test: `library-server/src/test/java/com/library/service/NoteServiceTest.java`

- [ ] **Step 1: 编写 NoteService**

```java
package com.library.service;

import com.library.dto.*;
import com.library.entity.Note;
import com.library.entity.NoteLike;
import com.library.entity.User;
import com.library.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteLikeRepository noteLikeRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final NotificationService notificationService;

    public NoteService(NoteRepository noteRepository,
                       NoteLikeRepository noteLikeRepository,
                       UserRepository userRepository,
                       BookRepository bookRepository,
                       UserBookRepository userBookRepository,
                       NotificationService notificationService) {
        this.noteRepository = noteRepository;
        this.noteLikeRepository = noteLikeRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userBookRepository = userBookRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public NoteResponse createNote(Long bookId, Long userId, NoteRequest req) {
        Note note = Note.builder()
            .bookId(bookId).userId(userId)
            .content(req.getContent())
            .selectedText(req.getSelectedText())
            .cfi(req.getCfi())
            .type(req.getType())
            .published(req.isPublish())
            .likeCount(0).replyCount(0)
            .build();
        note = noteRepository.save(note);
        return toResponse(note, Collections.emptyMap(), Collections.emptySet(),
            new HashMap<>(), new HashMap<>());
    }

    // 思余列表
    public PageResult<NoteResponse> getMyNotes(Long userId, String type,
                                                int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Note> notePage;
        if (type != null && !type.isEmpty()) {
            notePage = noteRepository
                .findByUserIdAndParentIdIsNullAndPublishedFalseAndTypeOrderByCreatedAtDesc(
                    userId, type, pageable);
        } else {
            notePage = noteRepository
                .findByUserIdAndParentIdIsNullAndPublishedFalseOrderByCreatedAtDesc(
                    userId, pageable);
        }
        List<NoteResponse> records = notePage.getContent().stream()
            .map(n -> toResponse(n, Collections.emptyMap(), Collections.emptySet(),
                new HashMap<>(), new HashMap<>()))
            .collect(Collectors.toList());
        return PageResult.<NoteResponse>builder()
            .records(records).total(notePage.getTotalElements())
            .page(page).size(size).build();
    }

    // 某本书下我的思余
    public List<NoteResponse> getMyNotesForBook(Long userId, Long bookId) {
        return noteRepository
            .findByUserIdAndBookIdAndParentIdIsNullAndPublishedFalseOrderByCreatedAtDesc(
                userId, bookId)
            .stream()
            .map(n -> toResponse(n, Collections.emptyMap(), Collections.emptySet(),
                new HashMap<>(), new HashMap<>()))
            .collect(Collectors.toList());
    }

    // 余音广场（随机池）
    public PageResult<NoteResponse> getPublicNotes(Long currentUserId, int page, int size) {
        // 获取用户书架中的书籍ID
        List<Long> shelfBookIds = Collections.emptyList();
        if (currentUserId != null) {
            shelfBookIds = userBookRepository.findByUserId(currentUserId).stream()
                .map(ub -> ub.getBook().getId())
                .collect(Collectors.toList());
        }

        int shelfTarget = (int) Math.round(size * 0.4);
        int otherTarget = size - shelfTarget;
        List<Note> pool = new ArrayList<>();

        if (!shelfBookIds.isEmpty()) {
            Pageable shelfPageable = PageRequest.of(0, shelfTarget);
            List<Note> shelfNotes = noteRepository
                .findByParentIdIsNullAndPublishedTrueAndBookIdIn(shelfBookIds, shelfPageable)
                .getContent();
            pool.addAll(shelfNotes);
        }

        Pageable otherPageable = PageRequest.of(0, otherTarget);
        List<Note> otherNotes;
        if (!shelfBookIds.isEmpty()) {
            otherNotes = noteRepository
                .findByParentIdIsNullAndPublishedTrueAndBookIdNotIn(shelfBookIds, otherPageable)
                .getContent();
        } else {
            otherNotes = noteRepository
                .findByParentIdIsNullAndPublishedTrue(otherPageable)
                .getContent();
        }
        pool.addAll(otherNotes);

        Collections.shuffle(pool);

        Set<Long> noteIds = pool.stream().map(Note::getId).collect(Collectors.toSet());
        Set<Long> likedIds = Collections.emptySet();
        if (currentUserId != null) {
            likedIds = noteLikeRepository
                .findByUserIdAndNoteIdIn(currentUserId, new ArrayList<>(noteIds))
                .stream().map(NoteLike::getNoteId).collect(Collectors.toSet());
        }

        Map<Long, String> usernameCache = new HashMap<>();
        Map<Long, String> bookTitleCache = new HashMap<>();

        List<NoteResponse> records = pool.stream()
            .map(n -> toResponse(n, Collections.emptyMap(), likedIds,
                usernameCache, bookTitleCache))
            .collect(Collectors.toList());

        return PageResult.<NoteResponse>builder()
            .records(records).total(records.size())
            .page(page).size(size).build();
    }

    // 某本书的公开手记
    public PageResult<NoteResponse> getPublicNotesForBook(Long bookId, Long currentUserId,
                                                           int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size,
            Sort.by("createdAt").descending());
        Page<Note> notePage = noteRepository
            .findByBookIdAndParentIdIsNullAndPublishedTrueOrderByCreatedAtDesc(
                bookId, pageable);

        List<Long> noteIds = notePage.getContent().stream()
            .map(Note::getId).collect(Collectors.toList());

        Map<Long, List<Note>> childrenMap = new HashMap<>();
        for (Long id : noteIds) {
            List<Note> replies = noteRepository.findByRootIdOrderByCreatedAtAsc(id);
            for (Note r : replies) {
                childrenMap.computeIfAbsent(r.getParentId(), k -> new ArrayList<>()).add(r);
            }
        }

        Set<Long> allIds = new HashSet<>(noteIds);
        childrenMap.values().forEach(list -> list.forEach(r -> allIds.add(r.getId())));

        Set<Long> likedIds = Collections.emptySet();
        if (currentUserId != null) {
            likedIds = noteLikeRepository
                .findByUserIdAndNoteIdIn(currentUserId, new ArrayList<>(allIds))
                .stream().map(NoteLike::getNoteId).collect(Collectors.toSet());
        }

        Map<Long, String> usernameCache = new HashMap<>();
        Map<Long, String> bookTitleCache = new HashMap<>();

        List<NoteResponse> records = notePage.getContent().stream()
            .map(n -> toResponse(n, childrenMap, likedIds, usernameCache, bookTitleCache))
            .collect(Collectors.toList());

        return PageResult.<NoteResponse>builder()
            .records(records).total(notePage.getTotalElements())
            .page(page).size(size).build();
    }

    @Transactional
    public NoteResponse updateNote(Long noteId, Long userId, NoteRequest req) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + noteId));
        if (!note.getUserId().equals(userId)) {
            throw new SecurityException("无权编辑他人手记");
        }
        long minutes = ChronoUnit.MINUTES.between(note.getCreatedAt(), LocalDateTime.now());
        if (minutes >= 3) {
            throw new IllegalStateException("编辑时间已过（3分钟内可编辑）");
        }
        note.setContent(req.getContent());
        if (req.getType() != null) note.setType(req.getType());
        note = noteRepository.save(note);
        return toResponse(note, Collections.emptyMap(), Collections.emptySet(),
            new HashMap<>(), new HashMap<>());
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId, boolean isAdmin) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + noteId));
        if (!isAdmin && !note.getUserId().equals(userId)) {
            throw new SecurityException("无权删除他人手记");
        }
        noteRepository.delete(note);
    }

    @Transactional
    public NoteResponse publishNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + noteId));
        if (!note.getUserId().equals(userId)) {
            throw new SecurityException("无权操作他人手记");
        }
        note.setPublished(true);
        note = noteRepository.save(note);
        return toResponse(note, Collections.emptyMap(), Collections.emptySet(),
            new HashMap<>(), new HashMap<>());
    }

    @Transactional
    public NoteResponse unpublishNote(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + noteId));
        if (!note.getUserId().equals(userId)) {
            throw new SecurityException("无权操作他人手记");
        }
        note.setPublished(false);
        note = noteRepository.save(note);
        return toResponse(note, Collections.emptyMap(), Collections.emptySet(),
            new HashMap<>(), new HashMap<>());
    }

    @Transactional
    public NoteResponse createReply(Long parentId, Long userId, NoteRequest req) {
        Note parent = noteRepository.findById(parentId)
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + parentId));
        if (!parent.isPublished()) {
            throw new IllegalStateException("只能回复已公开的手记");
        }
        Long rootId = parent.getParentId() == null ? parentId : parent.getRootId();
        Note reply = Note.builder()
            .bookId(parent.getBookId()).userId(userId)
            .parentId(parentId).rootId(rootId)
            .content(req.getContent())
            .type(parent.getType())
            .published(true)
            .likeCount(0).replyCount(0)
            .build();
        reply = noteRepository.save(reply);
        noteRepository.incrementReplyCount(rootId);

        if (!parent.getUserId().equals(userId)) {
            String replierName = userRepository.findById(userId)
                .map(User::getUsername).orElse("未知用户");
            String snippet = req.getContent().length() > 30
                ? req.getContent().substring(0, 30) + "..." : req.getContent();
            notificationService.createNotification(parent.getUserId(), "note_reply",
                replierName + " 回复了你的手记",
                "回复内容：「" + snippet + "」",
                parent.getBookId(), 0L);
        }
        return toResponse(reply, Collections.emptyMap(), Collections.emptySet(),
            new HashMap<>(), new HashMap<>());
    }

    @Transactional
    public boolean toggleLike(Long noteId, Long userId) {
        Note note = noteRepository.findById(noteId)
            .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + noteId));
        if (!note.isPublished()) {
            throw new IllegalStateException("只能点赞已公开的手记");
        }
        var existing = noteLikeRepository.findByNoteIdAndUserId(noteId, userId);
        if (existing.isPresent()) {
            noteLikeRepository.delete(existing.get());
            noteRepository.decrementLikeCount(noteId);
            return false;
        } else {
            noteLikeRepository.save(new NoteLike(noteId, userId));
            noteRepository.incrementLikeCount(noteId);
            return true;
        }
    }

    private NoteResponse toResponse(Note note,
                                     Map<Long, List<Note>> childrenMap,
                                     Set<Long> likedIds,
                                     Map<Long, String> usernameCache,
                                     Map<Long, String> bookTitleCache) {
        String username = usernameCache.computeIfAbsent(note.getUserId(), uid ->
            userRepository.findById(uid).map(User::getUsername).orElse("未知用户"));
        String bookTitle = bookTitleCache.computeIfAbsent(note.getBookId(), bid ->
            bookRepository.findById(bid).map(b -> b.getTitle()).orElse("未知图书"));

        List<NoteResponse> replies = childrenMap.getOrDefault(note.getId(), Collections.emptyList())
            .stream()
            .map(r -> toResponse(r, childrenMap, likedIds, usernameCache, bookTitleCache))
            .collect(Collectors.toList());

        return NoteResponse.builder()
            .id(note.getId()).userId(note.getUserId()).username(username)
            .bookId(note.getBookId()).bookTitle(bookTitle)
            .content(note.getContent()).selectedText(note.getSelectedText())
            .cfi(note.getCfi()).type(note.getType())
            .published(note.isPublished())
            .parentId(note.getParentId()).rootId(note.getRootId())
            .likeCount(note.getLikeCount()).replyCount(note.getReplyCount())
            .liked(likedIds.contains(note.getId()))
            .createdAt(note.getCreatedAt()).updatedAt(note.getUpdatedAt())
            .replies(replies)
            .build();
    }
}
```

- [ ] **Step 2: 编写 NoteServiceTest**

```java
package com.library.service;

import com.library.dto.NoteRequest;
import com.library.dto.NoteResponse;
import com.library.dto.PageResult;
import com.library.entity.*;
import com.library.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private NoteLikeRepository noteLikeRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserBookRepository userBookRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private NoteService noteService;

    private Note note;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testuser", "pass", "reader", null);
        note = Note.builder()
            .id(1L).userId(1L).bookId(10L)
            .content("这段意境真美").selectedText("落霞与孤鹜齐飞")
            .cfi("epubcfi(/6/4)").type("INSIGHT")
            .published(false).likeCount(0).replyCount(0)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(
            com.library.entity.Book.builder().id(10L).title("滕王阁序").build()));
    }

    @Test
    void shouldCreateNote() {
        NoteRequest req = new NoteRequest();
        req.setContent("这段意境真美");
        req.setSelectedText("落霞与孤鹜齐飞");
        req.setCfi("epubcfi(/6/4)");
        req.setType("INSIGHT");
        req.setPublish(false);

        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteResponse result = noteService.createNote(10L, 1L, req);

        assertThat(result.getContent()).isEqualTo("这段意境真美");
        assertThat(result.getSelectedText()).isEqualTo("落霞与孤鹜齐飞");
        assertThat(result.getType()).isEqualTo("INSIGHT");
        assertThat(result.isPublished()).isFalse();
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void shouldPublishNote() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        note.setPublished(true);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteResponse result = noteService.publishNote(1L, 1L);

        assertThat(result.isPublished()).isTrue();
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `cd library-server && mvn test -Dtest=NoteServiceTest -pl .`
Expected: PASS (2 tests)

- [ ] **Step 4: 提交**

```bash
git add library-server/src/main/java/com/library/service/NoteService.java \
        library-server/src/test/java/com/library/service/NoteServiceTest.java
git commit -m "feat: add NoteService with CRUD, publish, reply, and like logic"
```

---

### Task 6: NoteController

**Files:**
- Create: `library-server/src/main/java/com/library/controller/NoteController.java`
- Test: `library-server/src/test/java/com/library/controller/NoteControllerTest.java`

- [ ] **Step 1: 编写 NoteController**

```java
package com.library.controller;

import com.library.dto.*;
import com.library.service.NoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) { this.noteService = noteService; }

    @PostMapping("/books/{bookId}/notes")
    public ApiResponse<NoteResponse> createNote(@PathVariable Long bookId,
            @Valid @RequestBody NoteRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.createNote(bookId, userId, request));
    }

    @GetMapping("/notes/mine")
    public ApiResponse<PageResult<NoteResponse>> getMyNotes(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getMyNotes(userId, type, page, size));
    }

    @GetMapping("/books/{bookId}/notes/mine")
    public ApiResponse<List<NoteResponse>> getMyNotesForBook(@PathVariable Long bookId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getMyNotesForBook(userId, bookId));
    }

    @PutMapping("/notes/{id}")
    public ApiResponse<NoteResponse> updateNote(@PathVariable Long id,
            @Valid @RequestBody NoteRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.updateNote(id, userId, request));
    }

    @DeleteMapping("/notes/{id}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        noteService.deleteNote(id, userId, false);
        return ApiResponse.success(null);
    }

    @PostMapping("/notes/{id}/publish")
    public ApiResponse<NoteResponse> publishNote(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.publishNote(id, userId));
    }

    @PostMapping("/notes/{id}/unpublish")
    public ApiResponse<NoteResponse> unpublishNote(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.unpublishNote(id, userId));
    }

    @GetMapping("/notes/public")
    public ApiResponse<PageResult<NoteResponse>> getPublicNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getPublicNotes(userId, page, size));
    }

    @GetMapping("/books/{bookId}/notes/public")
    public ApiResponse<PageResult<NoteResponse>> getPublicNotesForBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.getPublicNotesForBook(bookId, userId, page, size));
    }

    @PostMapping("/notes/{id}/reply")
    public ApiResponse<NoteResponse> createReply(@PathVariable Long id,
            @Valid @RequestBody NoteRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.createReply(id, userId, request));
    }

    @PostMapping("/notes/{id}/like")
    public ApiResponse<Boolean> toggleLike(@PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return ApiResponse.success(noteService.toggleLike(id, userId));
    }
}
```

- [ ] **Step 2: 编写 NoteControllerTest**

```java
package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.NoteRequest;
import com.library.dto.NoteResponse;
import com.library.dto.PageResult;
import com.library.service.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class NoteControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private NoteService noteService;

    @Test
    void shouldCreateNote() throws Exception {
        NoteRequest req = new NoteRequest();
        req.setContent("这段意境真美");
        req.setSelectedText("落霞与孤鹜齐飞");
        req.setType("INSIGHT");

        NoteResponse resp = NoteResponse.builder()
            .id(1L).userId(1L).bookId(10L)
            .content("这段意境真美").selectedText("落霞与孤鹜齐飞")
            .type("INSIGHT").published(false).build();

        when(noteService.createNote(anyLong(), anyLong(), any()))
            .thenReturn(resp);

        mockMvc.perform(post("/api/books/10/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr("userId", 1L)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("这段意境真美"));
    }

    @Test
    void shouldReturnMyNotes() throws Exception {
        PageResult<NoteResponse> page = PageResult.<NoteResponse>builder()
            .records(Collections.emptyList()).total(0).page(1).size(20).build();
        when(noteService.getMyNotes(anyLong(), any(), anyInt(), anyInt()))
            .thenReturn(page);

        mockMvc.perform(get("/api/notes/mine")
                .requestAttr("userId", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 3: 运行测试**

Run: `cd library-server && mvn test -Dtest=NoteControllerTest -pl .`
Expected: PASS

- [ ] **Step 4: 提交**

```bash
git add library-server/src/main/java/com/library/controller/NoteController.java \
        library-server/src/test/java/com/library/controller/NoteControllerTest.java
git commit -m "feat: add NoteController with full REST API"
```

---

### Task 7: 扩展 Report 系统支持 Note 举报

**Files:**
- Modify: `library-server/src/main/java/com/library/entity/Report.java`
- Modify: `library-server/src/main/java/com/library/repository/ReportRepository.java`
- Modify: `library-server/src/main/java/com/library/dto/ReportRequest.java`
- Modify: `library-server/src/main/java/com/library/dto/ReportResponse.java`
- Modify: `library-server/src/main/java/com/library/service/ReportService.java`
- Modify: `library-server/src/main/java/com/library/controller/ReportController.java`

- [ ] **Step 1: 扩展 Report 实体 — 添加 noteId 和 targetType 字段**

在 `Report.java` 的 `reviewId` 字段后添加：

```java
@Column(name = "note_id")
private Long noteId;

@Column(nullable = false, length = 20)
private String targetType = "review";
```

在 getter/setter 区域添加：

```java
public Long getNoteId() { return noteId; }
public void setNoteId(Long noteId) { this.noteId = noteId; }
public String getTargetType() { return targetType; }
public void setTargetType(String targetType) { this.targetType = targetType; }
```

- [ ] **Step 2: 扩展 ReportRepository — 添加 note 举报去重方法**

在 `ReportRepository.java` 中添加：

```java
boolean existsByNoteIdAndReporterId(Long noteId, Long reporterId);
```

- [ ] **Step 3: 扩展 ReportRequest — 添加 noteId 和 targetType**

在 `ReportRequest.java` 中添加：

```java
private Long noteId;

private String targetType = "review";

public Long getNoteId() { return noteId; }
public void setNoteId(Long noteId) { this.noteId = noteId; }
public String getTargetType() { return targetType; }
public void setTargetType(String targetType) { this.targetType = targetType; }
```

- [ ] **Step 4: 扩展 ReportResponse — 添加 noteId、noteContent、targetType**

在 `ReportResponse.java` 中添加字段和 builder 方法：

```java
private Long noteId;
private String noteContent;
private String targetType;

// getter/setter
public Long getNoteId() { return noteId; }
public void setNoteId(Long noteId) { this.noteId = noteId; }
public String getNoteContent() { return noteContent; }
public void setNoteContent(String noteContent) { this.noteContent = noteContent; }
public String getTargetType() { return targetType; }
public void setTargetType(String targetType) { this.targetType = targetType; }

// Builder 中添加:
public Builder noteId(Long v) { noteId = v; return this; }
public Builder noteContent(String v) { noteContent = v; return this; }
public Builder targetType(String v) { targetType = v; return this; }

// build() 中添加:
r.setNoteId(noteId); r.setNoteContent(noteContent); r.setTargetType(targetType);
```

- [ ] **Step 5: 扩展 ReportService — 支持 note 举报的创建、列表和解决**

`createReport` 方法改为根据 targetType 分别处理。在 `ReportService.java` 中重写 `createReport`:

```java
@Transactional
public void createReport(ReportRequest req, Long reporterId) {
    if ("note".equals(req.getTargetType())) {
        createNoteReport(req, reporterId);
    } else {
        createReviewReport(req, reporterId);
    }
}

private void createReviewReport(ReportRequest req, Long reporterId) {
    if (reportRepository.existsByReviewIdAndReporterId(req.getReviewId(), reporterId)) {
        throw new IllegalStateException("您已经举报过这条书评");
    }
    Review review = reviewRepository.findById(req.getReviewId())
        .orElseThrow(() -> new EntityNotFoundException("书评不存在: " + req.getReviewId()));
    if (review.getUserId().equals(reporterId)) {
        throw new IllegalStateException("不能举报自己的书评");
    }
    if ("other".equals(req.getReason()) && (req.getDetail() == null || req.getDetail().trim().isEmpty())) {
        throw new IllegalArgumentException("选择其他时必须填写补充说明");
    }
    Report report = Report.builder()
        .reviewId(req.getReviewId()).reporterId(reporterId)
        .reason(req.getReason()).detail(req.getDetail())
        .targetType("review").status("pending").build();
    reportRepository.save(report);
}

private void createNoteReport(ReportRequest req, Long reporterId) {
    if (reportRepository.existsByNoteIdAndReporterId(req.getNoteId(), reporterId)) {
        throw new IllegalStateException("您已经举报过这条手记");
    }
    Note note = noteRepository.findById(req.getNoteId())
        .orElseThrow(() -> new EntityNotFoundException("手记不存在: " + req.getNoteId()));
    if (note.getUserId().equals(reporterId)) {
        throw new IllegalStateException("不能举报自己的手记");
    }
    if ("other".equals(req.getReason()) && (req.getDetail() == null || req.getDetail().trim().isEmpty())) {
        throw new IllegalArgumentException("选择其他时必须填写补充说明");
    }
    Report report = Report.builder()
        .noteId(req.getNoteId()).reporterId(reporterId)
        .reason(req.getReason()).detail(req.getDetail())
        .targetType("note").status("pending").build();
    reportRepository.save(report);
}
```

`toResponse` 方法中根据 targetType 加载对应内容。
需要在构造函数中注入 `NoteRepository`：

```java
private final NoteRepository noteRepository;

public ReportService(ReportRepository reportRepository, ReviewRepository reviewRepository,
                     NoteRepository noteRepository, NotificationService notificationService,
                     UserRepository userRepository, BookRepository bookRepository) {
    // ... existing assignments ...
    this.noteRepository = noteRepository;
}
```

`resolveReport` 方法中增加 `note` 类型的处理（通知内容适配手记场景）。

- [ ] **Step 6: 扩展 ReportController — 适配新的 createReport 签名**

`ReportController.java` 的 `createReport` 改为：

```java
@PostMapping("/api/reports")
public ApiResponse<Void> createReport(@Valid @RequestBody ReportRequest request,
        HttpServletRequest httpRequest) {
    Long userId = (Long) httpRequest.getAttribute("userId");
    reportService.createReport(request, userId);
    return ApiResponse.success(null);
}
```

注意：原路由 `/api/reviews/{id}/report` 保留兼容，新增通用 `/api/reports` 端点。

- [ ] **Step 7: 提交**

```bash
git add library-server/src/main/java/com/library/entity/Report.java \
        library-server/src/main/java/com/library/repository/ReportRepository.java \
        library-server/src/main/java/com/library/dto/ReportRequest.java \
        library-server/src/main/java/com/library/dto/ReportResponse.java \
        library-server/src/main/java/com/library/service/ReportService.java \
        library-server/src/main/java/com/library/controller/ReportController.java
git commit -m "feat: extend Report system to support note reports"
```

---

### Task 8: 扩展 Notification 支持 note_id

**Files:**
- Modify: `library-server/src/main/java/com/library/entity/Notification.java`
- Modify: `library-server/src/main/java/com/library/dto/NotificationResponse.java`
- Modify: `library-server/src/main/java/com/library/service/NotificationService.java`

- [ ] **Step 1: 扩展 Notification 实体**

添加字段：

```java
@Column(name = "note_id")
private Long noteId;
```

构造器增加 `noteId` 参数，重载一个兼容旧签名的构造器。getter/setter 一并添加。

- [ ] **Step 2: 扩展 NotificationResponse**

添加字段：

```java
private Long noteId;

public Long getNoteId() { return noteId; }
public void setNoteId(Long noteId) { this.noteId = noteId; }
```

`toResponse` 中设置 `r.setNoteId(n.getNoteId())`。

- [ ] **Step 3: NotificationService 增加 note 相关通知方法**

```java
@Transactional
public void createNoteNotification(Long userId, String type, String title,
                                    String content, Long bookId, Long noteId) {
    Notification n = new Notification(userId, type, title, content, bookId, 0L);
    n.setNoteId(noteId);
    notificationRepository.save(n);
}
```

- [ ] **Step 4: 提交**

```bash
git add library-server/src/main/java/com/library/entity/Notification.java \
        library-server/src/main/java/com/library/dto/NotificationResponse.java \
        library-server/src/main/java/com/library/service/NotificationService.java
git commit -m "feat: extend Notification to support note_id"
```

---

### Task 9: 前端 API 模块

**Files:**
- Create: `reader-app/src/api/note.js`
- Modify: `reader-app/src/api/report.js`

- [ ] **Step 1: 编写 api/note.js**

```javascript
import api from './index'

export function createNote(bookId, data) {
  return api.post(`/books/${bookId}/notes`, data)
}

export function getMyNotes(params = {}) {
  return api.get('/notes/mine', { params })
}

export function getMyNotesForBook(bookId) {
  return api.get(`/books/${bookId}/notes/mine`)
}

export function updateNote(noteId, data) {
  return api.put(`/notes/${noteId}`, data)
}

export function deleteNote(noteId) {
  return api.delete(`/notes/${noteId}`)
}

export function publishNote(noteId) {
  return api.post(`/notes/${noteId}/publish`)
}

export function unpublishNote(noteId) {
  return api.post(`/notes/${noteId}/unpublish`)
}

export function getPublicNotes(params = {}) {
  return api.get('/notes/public', { params })
}

export function getPublicNotesForBook(bookId, params = {}) {
  return api.get(`/books/${bookId}/notes/public`, { params })
}

export function replyNote(noteId, data) {
  return api.post(`/notes/${noteId}/reply`, data)
}

export function toggleLikeNote(noteId) {
  return api.post(`/notes/${noteId}/like`)
}
```

- [ ] **Step 2: 扩展 api/report.js**

添加手记举报方法：

```javascript
export function reportNote(noteId, data) {
  return api.post('/reports', { ...data, noteId, targetType: 'note' })
}
```

保留原有 `reportReview` 方法，同时修改为使用新通用端点（可选，保持向后兼容）。

- [ ] **Step 3: 提交**

```bash
git add reader-app/src/api/note.js reader-app/src/api/report.js
git commit -m "feat: add note API module and extend report API for notes"
```

---

### Task 10: 路由更新 + BannerHeader 导航修改

**Files:**
- Modify: `reader-app/src/router/index.js`
- Modify: `reader-app/src/components/BannerHeader.vue`

- [ ] **Step 1: 更新 router/index.js**

添加路由：

```javascript
{ path: '/notes', name: 'notes', component: () => import('../views/Notes.vue'), meta: { requiresAuth: true } },
{ path: '/guide', name: 'guide', component: () => import('../views/Guide.vue') }
```

- [ ] **Step 2: 更新 BannerHeader.vue — 导航链接**

将模板中的：

```html
<span class="nav-item">分类浏览</span>
<span class="nav-sep">|</span>
<span class="nav-item">最新上架</span>
```

替换为：

```html
<span class="nav-item" :class="{ active: $route.path === '/notes' }" @click="$router.push('/notes')">书余</span>
<span class="nav-sep">|</span>
<span class="nav-item" :class="{ active: $route.path === '/guide' }" @click="$router.push('/guide')">凡例</span>
```

注意：首页链接也需添加 active 判断：

```html
<span class="nav-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">首页</span>
```

- [ ] **Step 3: 提交**

```bash
git add reader-app/src/router/index.js reader-app/src/components/BannerHeader.vue
git commit -m "feat: add /notes and /guide routes, update nav links"
```

---

### Task 11: Guide.vue（凡例静态页）

**Files:**
- Create: `reader-app/src/views/Guide.vue`

- [ ] **Step 1: 编写 Guide.vue**

```vue
<template>
  <div class="guide-page">
    <BannerHeader />
    <div class="guide-container">
      <h1 class="guide-title">凡例</h1>

      <section class="guide-section">
        <h2>使用指南</h2>

        <h3>开始阅读</h3>
        <p>在首页浏览或搜索感兴趣的书籍，点击书籍卡片进入详情页。在详情页点击"开始阅读"进入阅读器，你可以通过顶部工具栏调整字号、切换章节、翻页阅读。</p>

        <h3>记书余</h3>
        <p>阅读过程中，选中一段文字会弹出操作栏。点击"记书余"，在弹出的浮层中写下你的心得或疑问，选择类型标签后点击"留墨"即可保存。你的手记默认保存在思余中，仅自己可见。</p>

        <h3>思余与余音</h3>
        <p><strong>思余</strong>是你的私密手记空间，所有未公开的手记保存在这里，你可以随时查看、编辑或删除。当你觉得某条手记值得分享时，点击"送入余音"即可将其公开发布。</p>
        <p><strong>余音</strong>是公开手记广场，展示所有用户投递的手记。你可以浏览他人的读书心得，进行回复讨论和点赞。</p>

        <h3>写书评</h3>
        <p>在书籍详情页底部，你可以发表书评或回复他人的书评。书评支持点赞功能。</p>

        <h3>书架与收件箱</h3>
        <p>点击"我的书架"查看已收藏的书籍。点击"收件箱"查看回复和通知消息。</p>
      </section>

      <section class="guide-section">
        <h2>社区规范</h2>

        <h3>书评准则</h3>
        <ul>
          <li>尊重原著和作者，发表有实质内容的书评</li>
          <li>禁止人身攻击、辱骂或歧视性言论</li>
          <li>禁止发布广告、垃圾信息或与书籍无关的内容</li>
        </ul>

        <h3>手记发布要求</h3>
        <ul>
          <li>手记应为原创内容，与引用原文相关</li>
          <li>禁止抄袭他人手记或书评</li>
          <li>手记内容应围绕阅读讨论，不偏离主题</li>
          <li>禁止在手记中发布违法或违规内容</li>
        </ul>

        <h3>举报规则</h3>
        <p>如果你发现书评或手记存在以下问题，可以使用举报功能：</p>
        <ul>
          <li>垃圾广告</li>
          <li>人身攻击</li>
          <li>虚假信息</li>
          <li>违规内容</li>
          <li>其他（需附补充说明）</li>
        </ul>
        <p>管理团队会在收到举报后及时审核处理。请合理使用举报功能，恶意举报将受到限制。</p>

        <h3>账号规范</h3>
        <ul>
          <li>每位用户只能注册一个账号</li>
          <li>禁止冒充他人或使用不当用户名</li>
          <li>违反社区规范可能导致账号被限制或注销</li>
        </ul>
      </section>
    </div>
  </div>
</template>

<script setup>
import BannerHeader from '../components/BannerHeader.vue'
</script>

<style scoped>
.guide-page {
  min-height: 100vh;
  background: var(--color-bg, #fafaf7);
}

.guide-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}

.guide-title {
  font-family: var(--font-serif);
  font-size: 28px;
  color: var(--color-text, #4a3d2f);
  text-align: center;
  margin-bottom: 40px;
  letter-spacing: 4px;
}

.guide-section {
  margin-bottom: 40px;
}

.guide-section h2 {
  font-size: 18px;
  color: var(--color-primary, #c9a96e);
  border-bottom: 1px solid var(--color-border, #e8e4dc);
  padding-bottom: 8px;
  margin-bottom: 20px;
}

.guide-section h3 {
  font-size: 15px;
  color: var(--color-text, #4a3d2f);
  margin: 20px 0 8px;
}

.guide-section p {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-secondary, #8b8070);
  margin-bottom: 12px;
}

.guide-section ul {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-secondary, #8b8070);
  padding-left: 20px;
  margin-bottom: 12px;
}

.guide-section li {
  margin-bottom: 4px;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add reader-app/src/views/Guide.vue
git commit -m "feat: add Guide page with usage guide and community rules"
```

---

### Task 12: Notes.vue（书余主页 — 思余 + 余音）

**Files:**
- Create: `reader-app/src/views/Notes.vue`

- [ ] **Step 1: 编写 Notes.vue**

```vue
<template>
  <div class="notes-page">
    <BannerHeader />
    <div class="notes-container">
      <h1 class="notes-title">书余</h1>

      <div class="notes-tabs">
        <button
          :class="['tab-btn', { active: activeTab === 'siyu' }]"
          @click="activeTab = 'siyu'"
        >思余</button>
        <button
          :class="['tab-btn', { active: activeTab === 'yuyin' }]"
          @click="activeTab = 'yuyin'"
        >余音</button>
      </div>

      <!-- 思余 Tab -->
      <div v-if="activeTab === 'siyu'" class="siyu-tab">
        <div class="siyu-filter">
          <select v-model="noteType" @change="fetchMyNotes">
            <option value="">全部类型</option>
            <option value="QUESTION">疑问</option>
            <option value="INSIGHT">心得</option>
          </select>
        </div>

        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="myNotes.length === 0" class="empty-text">
          暂无手记。去读一本书，留下你的第一个书余吧。
        </div>
        <div v-else class="siyu-list">
          <div
            v-for="note in myNotes"
            :key="note.id"
            class="note-card"
          >
            <div class="note-header">
              <span class="note-book" @click="$router.push(`/book/${note.bookId}`)">
                《{{ note.bookTitle }}》
              </span>
              <span :class="['note-type-tag', note.type === 'QUESTION' ? 'tag-question' : 'tag-insight']">
                {{ note.type === 'QUESTION' ? '疑问' : '心得' }}
              </span>
            </div>
            <blockquote v-if="note.selectedText" class="note-quote">
              "{{ note.selectedText }}"
            </blockquote>
            <p class="note-content">{{ note.content }}</p>
            <div class="note-footer">
              <span class="note-time">{{ formatDate(note.createdAt) }}</span>
              <div class="note-actions">
                <button
                  v-if="!note.published"
                  class="btn-publish"
                  @click="handlePublish(note.id)"
                >送入余音</button>
                <button class="btn-edit" @click="showEditDialog(note)">编辑</button>
                <button class="btn-delete" @click="handleDelete(note.id)">删除</button>
              </div>
            </div>
          </div>
        </div>
        <Pagination
          v-if="myTotal > 0"
          :page="myPage" :total="myTotal" :size="20"
          @change="(p) => { myPage = p; fetchMyNotes() }"
        />
      </div>

      <!-- 余音 Tab -->
      <div v-if="activeTab === 'yuyin'" class="yuyin-tab">
        <div v-if="loading" class="loading-text">加载中...</div>
        <div v-else-if="publicNotes.length === 0" class="empty-text">
          余音广场暂无内容。
        </div>
        <div v-else class="yuyin-list">
          <div
            v-for="note in publicNotes"
            :key="note.id"
            class="note-card public"
          >
            <div class="note-header">
              <span class="note-user" @click="viewProfile(note.userId)">{{ note.username }}</span>
              <span class="note-book" @click="$router.push(`/book/${note.bookId}`)">
                于《{{ note.bookTitle }}》
              </span>
              <span :class="['note-type-tag', note.type === 'QUESTION' ? 'tag-question' : 'tag-insight']">
                {{ note.type === 'QUESTION' ? '疑问' : '心得' }}
              </span>
            </div>
            <blockquote v-if="note.selectedText" class="note-quote">
              "{{ note.selectedText }}"
            </blockquote>
            <p class="note-content">{{ note.content }}</p>
            <div class="note-footer">
              <span class="note-time">{{ formatDate(note.createdAt) }}</span>
              <div class="note-actions">
                <button
                  :class="['btn-like', { liked: note.liked }]"
                  @click="handleLike(note)"
                >{{ note.liked ? '已赞' : '点赞' }} {{ note.likeCount > 0 ? note.likeCount : '' }}</button>
                <button class="btn-reply" @click="showReplyDialog(note)">回复</button>
                <button class="btn-report" @click="showReportDialog(note)">举报</button>
              </div>
            </div>
            <!-- 回复列表 -->
            <div v-if="note.replies && note.replies.length > 0" class="replies-section">
              <div
                v-for="reply in note.replies"
                :key="reply.id"
                class="reply-item"
              >
                <span class="reply-user">{{ reply.username }}</span>
                <span class="reply-content">{{ reply.content }}</span>
                <span class="reply-time">{{ formatDate(reply.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <div v-if="editingNote" class="dialog-overlay" @click.self="editingNote = null">
      <div class="dialog-box">
        <h3>编辑手记</h3>
        <textarea v-model="editContent" rows="4" class="dialog-textarea"></textarea>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="editingNote = null">取消</button>
          <button class="btn-save" @click="handleSaveEdit">保存</button>
        </div>
      </div>
    </div>

    <!-- 回复对话框 -->
    <div v-if="replyingNote" class="dialog-overlay" @click.self="replyingNote = null">
      <div class="dialog-box">
        <h3>回复手记</h3>
        <p class="dialog-context">{{ replyingNote.username }}：{{ replyingNote.content.slice(0, 50) }}{{ replyingNote.content.length > 50 ? '...' : '' }}</p>
        <textarea v-model="replyContent" rows="3" class="dialog-textarea" placeholder="写下你的回复..."></textarea>
        <div class="dialog-actions">
          <button class="btn-cancel" @click="replyingNote = null">取消</button>
          <button class="btn-save" @click="handleReply">发送</button>
        </div>
      </div>
    </div>

    <ReportDialog
      v-if="reportNote"
      :visible="true"
      @close="reportNote = null"
      @submit="handleReport"
    />
    <UserProfileDialog
      v-if="profileUserId"
      :user-id="profileUserId"
      :visible="showProfile"
      @close="showProfile = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BannerHeader from '../components/BannerHeader.vue'
import Pagination from '../components/Pagination.vue'
import ReportDialog from '../components/ReportDialog.vue'
import UserProfileDialog from '../components/UserProfileDialog.vue'
import { getMyNotes, getPublicNotes, publishNote, updateNote, deleteNote, replyNote, toggleLikeNote } from '../api/note'
import { reportNote } from '../api/report'
import { formatDate } from '../utils/date'

const activeTab = ref('siyu')
const loading = ref(false)
const myNotes = ref([])
const myPage = ref(1)
const myTotal = ref(0)
const noteType = ref('')
const publicNotes = ref([])
const publicPage = ref(1)

// 编辑
const editingNote = ref(null)
const editContent = ref('')

// 回复
const replyingNote = ref(null)
const replyContent = ref('')

// 举报
const reportNote = ref(null)

// 用户主页
const showProfile = ref(false)
const profileUserId = ref(null)

onMounted(() => {
  if (activeTab.value === 'siyu') fetchMyNotes()
  else fetchPublicNotes()
})

async function fetchMyNotes() {
  loading.value = true
  try {
    const params = { page: myPage.value, size: 20 }
    if (noteType.value) params.type = noteType.value
    const res = await getMyNotes(params)
    myNotes.value = res.records
    myTotal.value = res.total
  } catch (e) {
    console.error('获取思余失败:', e)
  } finally {
    loading.value = false
  }
}

async function fetchPublicNotes() {
  loading.value = true
  try {
    const res = await getPublicNotes({ page: publicPage.value, size: 20 })
    publicNotes.value = res.records
  } catch (e) {
    console.error('获取余音失败:', e)
  } finally {
    loading.value = false
  }
}

async function handlePublish(noteId) {
  try {
    await publishNote(noteId)
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '投递失败'
    alert(msg)
  }
}

async function handleDelete(noteId) {
  if (!confirm('确定要删除这条手记吗？')) return
  try {
    await deleteNote(noteId)
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '删除失败'
    alert(msg)
  }
}

function showEditDialog(note) {
  editingNote.value = note
  editContent.value = note.content
}

async function handleSaveEdit() {
  if (!editContent.value.trim()) return
  try {
    await updateNote(editingNote.value.id, { content: editContent.value.trim() })
    editingNote.value = null
    fetchMyNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '编辑失败'
    alert(msg)
  }
}

function showReplyDialog(note) {
  replyingNote.value = note
  replyContent.value = ''
}

async function handleReply() {
  if (!replyContent.value.trim()) return
  try {
    await replyNote(replyingNote.value.rootId || replyingNote.value.id, {
      content: replyContent.value.trim()
    })
    replyingNote.value = null
    fetchPublicNotes()
  } catch (e) {
    const msg = e?.response?.data?.message || '回复失败'
    alert(msg)
  }
}

async function handleLike(note) {
  try {
    const liked = await toggleLikeNote(note.id)
    note.liked = liked
    note.likeCount += liked ? 1 : -1
  } catch (e) {
    const msg = e?.response?.data?.message || '操作失败'
    alert(msg)
  }
}

function showReportDialog(note) {
  reportNote.value = note
}

async function handleReport(reason, detail) {
  try {
    await reportNote(reportNote.value.id, { reason, detail })
    reportNote.value = null
    alert('举报已提交')
  } catch (e) {
    const msg = e?.response?.data?.message || '举报失败'
    alert(msg)
  }
}

function viewProfile(userId) {
  profileUserId.value = userId
  showProfile.value = true
}
</script>

<style scoped>
.notes-page {
  min-height: 100vh;
  background: var(--color-bg, #fafaf7);
}
.notes-container {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 24px 64px;
}
.notes-title {
  font-family: var(--font-serif);
  font-size: 28px;
  color: var(--color-text, #4a3d2f);
  text-align: center;
  margin-bottom: 24px;
  letter-spacing: 6px;
}
.notes-tabs {
  display: flex;
  justify-content: center;
  gap: 0;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--color-border, #e8e4dc);
}
.tab-btn {
  padding: 8px 32px;
  border: none;
  background: none;
  font-size: 15px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  font-family: var(--font-serif);
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}
.tab-btn.active {
  color: var(--color-primary, #c9a96e);
  border-bottom-color: var(--color-primary, #c9a96e);
}
.tab-btn:hover { color: var(--color-text, #4a3d2f); }

.siyu-filter {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}
.siyu-filter select {
  padding: 4px 12px;
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: 6px;
  font-size: 13px;
  color: var(--color-text, #4a3d2f);
  background: #fff;
}

.loading-text, .empty-text {
  text-align: center;
  padding: 40px;
  color: var(--color-text-muted, #a09880);
  font-size: 14px;
}

.note-card {
  background: #fff;
  border: 1px solid var(--color-border, #e8e4dc);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.note-card.public { border-left: 3px solid var(--color-primary, #c9a96e); }

.note-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.note-user {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
}
.note-user:hover { color: var(--color-primary, #c9a96e); }
.note-book {
  font-size: 13px;
  color: var(--color-primary, #c9a96e);
  cursor: pointer;
}
.note-book:hover { text-decoration: underline; }

.note-type-tag {
  font-size: 11px;
  padding: 1px 8px;
  border-radius: 10px;
}
.tag-question { background: #e8f4fd; color: #3a7db8; }
.tag-insight { background: #fef3e4; color: #b8860b; }

.note-quote {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  border-left: 2px solid #e0dbd0;
  padding-left: 10px;
  margin: 8px 0;
  font-style: italic;
}

.note-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--color-text, #4a3d2f);
  margin-bottom: 10px;
}

.note-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.note-time {
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
}
.note-actions {
  display: flex;
  gap: 8px;
}
.note-actions button {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 4px;
  border: 1px solid #e0dbd0;
  background: #fff;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  transition: all 0.15s;
}
.note-actions button:hover {
  border-color: var(--color-primary, #c9a96e);
  color: var(--color-primary, #c9a96e);
}
.btn-publish {
  color: var(--color-primary, #c9a96e) !important;
  border-color: var(--color-primary, #c9a96e) !important;
}
.btn-like.liked {
  background: var(--color-primary, #c9a96e) !important;
  color: #fff !important;
  border-color: transparent !important;
}
.btn-delete:hover { color: #c04040 !important; border-color: #c04040 !important; }

/* 回复区域 */
.replies-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0ebe0;
}
.reply-item {
  padding: 6px 0;
  font-size: 13px;
}
.reply-user {
  font-weight: 600;
  color: var(--color-text, #4a3d2f);
  margin-right: 8px;
}
.reply-content {
  color: var(--color-text-secondary, #8b8070);
}
.reply-time {
  font-size: 11px;
  color: var(--color-text-muted, #a09880);
  margin-left: 8px;
}

/* 对话框 */
.dialog-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.dialog-box {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  width: 90%;
  max-width: 420px;
}
.dialog-box h3 {
  font-size: 16px;
  margin-bottom: 12px;
  color: var(--color-text, #4a3d2f);
}
.dialog-context {
  font-size: 12px;
  color: var(--color-text-muted, #a09880);
  margin-bottom: 10px;
  padding: 8px;
  background: #fafaf7;
  border-radius: 4px;
}
.dialog-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0dbd0;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  font-family: var(--font-sans);
  color: var(--color-text, #4a3d2f);
}
.dialog-textarea:focus { outline: none; border-color: var(--color-primary, #c9a96e); }
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}
.dialog-actions button {
  padding: 6px 20px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.btn-cancel {
  background: #f5f5f5;
  border: 1px solid #e0dbd0;
  color: var(--color-text-secondary, #8b8070);
}
.btn-save {
  background: var(--color-primary, #c9a96e);
  border: none;
  color: #fff;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add reader-app/src/views/Notes.vue
git commit -m "feat: add Notes page with Siyu and Yuyin tabs"
```

---

### Task 13: BookReader.vue — 批注选择与撰写 UI

**Files:**
- Modify: `reader-app/src/views/BookReader.vue`

- [ ] **Step 1: 改造 BookReader.vue**

在现有阅读器基础上增加选区批注功能。关键改动点：

1. 模板中在 `reader-viewer` 上方添加操作浮泡和撰写浮层：

```html
<!-- 选区操作浮泡 -->
<div
  v-if="showBubble"
  class="selection-bubble"
  :style="bubbleStyle"
>
  <button @click="onCopy">复制</button>
  <button @click="onHighlight">划线</button>
  <button @click="onAnnotate">记书余</button>
</div>

<!-- 撰写浮层 -->
<div v-if="showAnnotate" class="annotate-panel">
  <div class="annotate-header">
    <button
      :class="['annotate-tab', { active: annotateType === 'QUESTION' }]"
      @click="annotateType = 'QUESTION'"
    >疑问</button>
    <button
      :class="['annotate-tab', { active: annotateType === 'INSIGHT' }]"
      @click="annotateType = 'INSIGHT'"
    >心得</button>
  </div>
  <blockquote class="annotate-quote">"{{ selectedText }}"</blockquote>
  <textarea
    v-model="annotateContent"
    class="annotate-textarea"
    :placeholder="annotateType === 'QUESTION' ? '你哪里困惑？' : '写下你的心得...'"
  ></textarea>
  <div class="annotate-footer">
    <label class="annotate-toggle">
      <input type="checkbox" v-model="syncToYuyin" />
      <span>同步至余音</span>
    </label>
    <button class="annotate-submit" @click="onSaveNote">留墨</button>
  </div>
</div>
```

2. Script 新增逻辑（在现有 script setup 中追加）：

```javascript
// 批注相关状态
const showBubble = ref(false)
const bubbleStyle = ref({})
const selectedText = ref('')
const selectedCfi = ref('')
const showAnnotate = ref(false)
const annotateType = ref('INSIGHT')
const annotateContent = ref('')
const syncToYuyin = ref(false)

let lastSelection = null

// 监听选区事件 — 在 rendition.display() 之后注册
rendition.on('selected', (cfiRange, contents) => {
  const selection = contents.window.getSelection()
  const text = selection.toString().trim()
  if (!text || text.length < 1) {
    showBubble.value = false
    return
  }
  selectedText.value = text
  selectedCfi.value = cfiRange
  lastSelection = selection

  const range = selection.getRangeAt(0)
  const rect = range.getBoundingClientRect()
  bubbleStyle.value = {
    left: `${rect.left + rect.width / 2 - 60}px`,
    top: `${rect.top - 40}px`
  }
  showBubble.value = true
})

// 点击其他区域关闭浮泡
rendition.on('click', () => {
  showBubble.value = false
})

function onCopy() {
  navigator.clipboard.writeText(selectedText.value)
  showBubble.value = false
}

function onHighlight() {
  if (rendition && lastSelection) {
    rendition.annotations.highlight(
      lastSelection.getRangeAt(0),
      {},
      null,
      'highlight'
    )
  }
  showBubble.value = false
}

function onAnnotate() {
  showBubble.value = false
  showAnnotate.value = true
  annotateContent.value = ''
  annotateType.value = 'INSIGHT'
  syncToYuyin.value = false
}

async function onSaveNote() {
  if (!annotateContent.value.trim()) return
  try {
    await createNote(bookId, {
      content: annotateContent.value.trim(),
      selectedText: selectedText.value,
      cfi: selectedCfi.value,
      type: annotateType.value,
      publish: syncToYuyin.value
    })
    showAnnotate.value = false
    // 在段落下方添加角标
    if (rendition && lastSelection) {
      rendition.annotations.highlight(
        lastSelection.getRangeAt(0),
        {},
        null,
        'annotation'
      )
    }
    // 微动效提示
    alert('留墨成功')
  } catch (e) {
    const msg = e?.response?.data?.message || '保存失败'
    alert(msg)
  }
}
```

3. import 中增加：

```javascript
import { createNote } from '../api/note'
```

4. 在 `<style scoped>` 区域追加样式：

```css
/* 选区操作浮泡 */
.selection-bubble {
  position: fixed;
  z-index: 50;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.12);
  display: flex;
  padding: 4px;
  gap: 2px;
}
.selection-bubble button {
  padding: 4px 12px;
  border: none;
  background: none;
  font-size: 12px;
  color: var(--color-text, #4a3d2f);
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s;
}
.selection-bubble button:hover { background: #f5f0e5; }

/* 撰写浮层 */
.annotate-panel {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 60;
  background: #fff;
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -2px 16px rgba(0,0,0,0.1);
  padding: 20px 24px 24px;
  max-height: 60vh;
  overflow-y: auto;
}
.annotate-header {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
}
.annotate-tab {
  padding: 4px 16px;
  border: 1px solid #e0dbd0;
  border-radius: 14px;
  background: none;
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
  transition: all 0.2s;
}
.annotate-tab.active {
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border-color: transparent;
}
.annotate-quote {
  font-size: 13px;
  color: var(--color-text-muted, #a09880);
  border-left: 2px solid #e0dbd0;
  padding-left: 10px;
  margin-bottom: 12px;
  font-style: italic;
}
.annotate-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0dbd0;
  border-radius: 8px;
  font-size: 14px;
  min-height: 80px;
  resize: vertical;
  font-family: var(--font-sans);
  color: var(--color-text, #4a3d2f);
}
.annotate-textarea:focus { outline: none; border-color: var(--color-primary, #c9a96e); }
.annotate-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}
.annotate-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary, #8b8070);
  cursor: pointer;
}
.annotate-submit {
  padding: 8px 28px;
  background: var(--color-primary, #c9a96e);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  font-family: var(--font-serif);
  letter-spacing: 2px;
}
.annotate-submit:hover { background: var(--color-primary-hover, #b8944d); }
```

- [ ] **Step 2: 提交**

```bash
git add reader-app/src/views/BookReader.vue
git commit -m "feat: add annotation selection and compose UI in BookReader"
```

---

### Task 14: 数据库迁移脚本

**Files:**
- Create: `library-server/src/main/resources/db/migration/V3__add_notes.sql`（或直接依赖 JPA ddl-auto）

- [ ] **Step 1: 编写 SQL 迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    selected_text VARCHAR(500),
    cfi VARCHAR(500),
    type VARCHAR(20) NOT NULL DEFAULT 'INSIGHT',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    parent_id BIGINT,
    root_id BIGINT,
    like_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);

CREATE TABLE IF NOT EXISTS note_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    UNIQUE KEY uk_note_user (note_id, user_id),
    FOREIGN KEY (note_id) REFERENCES notes(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE reports ADD COLUMN note_id BIGINT;
ALTER TABLE reports ADD COLUMN target_type VARCHAR(20) NOT NULL DEFAULT 'review';
ALTER TABLE notifications ADD COLUMN note_id BIGINT;
```

- [ ] **Step 2: 执行迁移并验证**

```bash
cd library-server && mvn flyway:migrate -pl .
```
或确认 `spring.jpa.hibernate.ddl-auto=update` 自动建表。

- [ ] **Step 3: 提交**

```bash
git add library-server/src/main/resources/db/migration/V3__add_notes.sql
git commit -m "feat: add database migration for notes and note_likes tables"
```

---

### Task 15: 集成测试与验证

**Files:**
- 无新增文件，端到端功能验证

- [ ] **Step 1: 启动后端**

```bash
cd library-server && mvn spring-boot:run
```
Expected: 应用启动成功，notes/note_likes 表自动创建

- [ ] **Step 2: 启动前端**

```bash
cd reader-app && npm run dev
```
Expected: Vite 开发服务器启动

- [ ] **Step 3: 手动验证清单**

1. 导航栏显示"书余"和"凡例" ✓
2. 点击凡例 → 显示使用指南和社区规范 ✓
3. 进入一本书的阅读器 → 选中文字 → 弹出操作浮泡（复制/划线/记书余） ✓
4. 点击记书余 → 半屏浮层弹出 → 切换疑问/心得 → 写内容 → 留墨 ✓
5. 去书余 → 思余 Tab → 看到刚写的手记 ✓
6. 思余中点击"送入余音" → 手记变为公开 ✓
7. 切换到余音 Tab → 看到公开手记 ✓
8. 余音中点赞、回复、举报 ✓

- [ ] **Step 4: 提交（如有微调）**

```bash
git add -A && git commit -m "chore: integration verification adjustments"
```

---

## 附录：依赖关系

```
Task 1 (Note Entity) → Task 2 (NoteLike) → Task 3 (Repositories)
                                           → Task 4 (DTOs)
Task 3 + 4 → Task 5 (NoteService)
Task 5 → Task 6 (NoteController)
Task 3 → Task 7 (Report extension) → Task 11 (Notes.vue)
Task 1 → Task 8 (Notification extension)
          Task 9 (Frontend API) → Task 12 (BookReader)
          Task 10 (Router/Nav)
                                 → Task 13 (Guide.vue, independent)
Task 6-13 → Task 14 (DB migration)
         → Task 15 (Integration test)
```

可并行组：
- Task 7 (Report扩展) 和 Task 8 (Notification扩展) 可并行
- Task 10 (路由/导航) 和 Task 13 (Guide.vue) 可并行
- Task 11 (Notes.vue) 和 Task 12 (BookReader.vue) 可并行（都依赖 Task 9）
