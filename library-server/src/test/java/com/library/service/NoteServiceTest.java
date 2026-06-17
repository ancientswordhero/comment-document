package com.library.service;

import com.library.dto.NoteRequest;
import com.library.dto.NoteResponse;
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
            Book.builder().id(10L).title("滕王阁序").build()));
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
