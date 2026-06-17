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
