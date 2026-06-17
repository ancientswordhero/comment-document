package com.library.repository;

import com.library.entity.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoteLikeRepository extends JpaRepository<NoteLike, Long> {

    Optional<NoteLike> findByNoteIdAndUserId(Long noteId, Long userId);

    List<NoteLike> findByUserIdAndNoteIdIn(Long userId, List<Long> noteIds);
}
