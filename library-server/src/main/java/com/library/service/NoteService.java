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

    public PageResult<NoteResponse> getMyNotes(Long userId, String type,
                                                int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Note> notePage;
        if (type != null && !type.isEmpty()) {
            notePage = noteRepository
                .findByUserIdAndParentIdIsNullAndTypeOrderByCreatedAtDesc(
                    userId, type, pageable);
        } else {
            notePage = noteRepository
                .findByUserIdAndParentIdIsNullOrderByCreatedAtDesc(
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

    public List<NoteResponse> getMyNotesForBook(Long userId, Long bookId) {
        return noteRepository
            .findByUserIdAndBookIdAndParentIdIsNullOrderByCreatedAtDesc(
                userId, bookId)
            .stream()
            .map(n -> toResponse(n, Collections.emptyMap(), Collections.emptySet(),
                new HashMap<>(), new HashMap<>()))
            .collect(Collectors.toList());
    }

    public PageResult<NoteResponse> getPublicNotes(Long currentUserId, int page, int size) {
        List<Long> shelfBookIds = Collections.emptyList();
        if (currentUserId != null) {
            shelfBookIds = userBookRepository.findByUserId(currentUserId).stream()
                .map(ub -> ub.getBookId())
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
        final Set<Long> finalLikedIds = likedIds;

        Map<Long, String> usernameCache = new HashMap<>();
        Map<Long, String> bookTitleCache = new HashMap<>();

        List<NoteResponse> records = pool.stream()
            .map(n -> toResponse(n, Collections.emptyMap(), finalLikedIds,
                usernameCache, bookTitleCache))
            .collect(Collectors.toList());

        return PageResult.<NoteResponse>builder()
            .records(records).total(records.size())
            .page(page).size(size).build();
    }

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
        final Set<Long> finalLikedIds = likedIds;

        Map<Long, String> usernameCache = new HashMap<>();
        Map<Long, String> bookTitleCache = new HashMap<>();

        List<NoteResponse> records = notePage.getContent().stream()
            .map(n -> toResponse(n, childrenMap, finalLikedIds, usernameCache, bookTitleCache))
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
                "回复内容：" + snippet + "",
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

        List<NoteResponse> replies = childrenMap
            .getOrDefault(note.getId(), Collections.emptyList())
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
