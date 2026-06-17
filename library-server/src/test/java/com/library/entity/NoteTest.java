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
