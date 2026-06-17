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

    // 思余：用户的所有手记（含已发布和未发布，非回复）
    Page<Note> findByUserIdAndParentIdIsNullOrderByCreatedAtDesc(
        Long userId, Pageable pageable);

    // 某本书下用户的思余
    List<Note> findByUserIdAndBookIdAndParentIdIsNullOrderByCreatedAtDesc(
        Long userId, Long bookId);

    // 按类型筛选思余
    Page<Note> findByUserIdAndParentIdIsNullAndTypeOrderByCreatedAtDesc(
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
