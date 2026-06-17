package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.library.config.JwtFilter;
import com.library.config.JwtUtil;
import com.library.config.SecurityConfig;
import com.library.config.WebConfig;
import com.library.dto.*;
import com.library.entity.User;
import com.library.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
@Import({JwtUtil.class, JwtFilter.class, SecurityConfig.class, WebConfig.class})
class NoteControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @MockBean private NoteService noteService;

    private String userToken;

    @BeforeEach
    void setUp() {
        User testUser = User.builder().id(5L).username("testuser").role("USER").build();
        userToken = jwtUtil.generateToken(testUser);
    }

    @Test
    void shouldCreateNote() throws Exception {
        NoteRequest req = new NoteRequest();
        req.setContent("这段意境真美");
        req.setSelectedText("落霞与孤鹜齐飞");
        req.setType("INSIGHT");

        NoteResponse resp = NoteResponse.builder()
            .id(1L).userId(5L).bookId(10L)
            .content("这段意境真美").selectedText("落霞与孤鹜齐飞")
            .type("INSIGHT").published(false).build();

        when(noteService.createNote(eq(10L), eq(5L), any()))
            .thenReturn(resp);

        mvc.perform(post("/api/books/10/notes")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("这段意境真美"));
    }

    @Test
    void shouldReturnMyNotes() throws Exception {
        PageResult<NoteResponse> page = PageResult.<NoteResponse>builder()
            .records(Collections.emptyList()).total(0).page(1).size(20).build();
        when(noteService.getMyNotes(eq(5L), isNull(), eq(1), eq(20)))
            .thenReturn(page);

        mvc.perform(get("/api/notes/mine")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturnMyNotesForBook() throws Exception {
        NoteResponse resp = NoteResponse.builder()
            .id(1L).userId(5L).bookId(10L)
            .content("好文").type("INSIGHT").published(false).build();
        when(noteService.getMyNotesForBook(eq(5L), eq(10L)))
            .thenReturn(Collections.singletonList(resp));

        mvc.perform(get("/api/books/10/notes/mine")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].content").value("好文"));
    }

    @Test
    void shouldUpdateNote() throws Exception {
        NoteRequest req = new NoteRequest();
        req.setContent("修改后的内容");
        req.setType("INSIGHT");

        NoteResponse resp = NoteResponse.builder()
            .id(1L).userId(5L).content("修改后的内容")
            .type("INSIGHT").published(false).build();
        when(noteService.updateNote(eq(1L), eq(5L), any())).thenReturn(resp);

        mvc.perform(put("/api/notes/1")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("修改后的内容"));
    }

    @Test
    void shouldDeleteNote() throws Exception {
        mvc.perform(delete("/api/notes/1")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldPublishNote() throws Exception {
        NoteResponse resp = NoteResponse.builder()
            .id(1L).userId(5L).content("公开手记")
            .type("INSIGHT").published(true).build();
        when(noteService.publishNote(eq(1L), eq(5L))).thenReturn(resp);

        mvc.perform(post("/api/notes/1/publish")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.published").value(true));
    }

    @Test
    void shouldUnpublishNote() throws Exception {
        NoteResponse resp = NoteResponse.builder()
            .id(1L).userId(5L).content("取消公开")
            .type("INSIGHT").published(false).build();
        when(noteService.unpublishNote(eq(1L), eq(5L))).thenReturn(resp);

        mvc.perform(post("/api/notes/1/unpublish")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.published").value(false));
    }

    @Test
    void shouldReturnPublicNotes() throws Exception {
        PageResult<NoteResponse> page = PageResult.<NoteResponse>builder()
            .records(Collections.emptyList()).total(0).page(1).size(20).build();
        when(noteService.getPublicNotes(eq(5L), eq(1), eq(20)))
            .thenReturn(page);

        mvc.perform(get("/api/notes/public")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturnPublicNotesForBook() throws Exception {
        PageResult<NoteResponse> page = PageResult.<NoteResponse>builder()
            .records(Collections.emptyList()).total(0).page(1).size(20).build();
        when(noteService.getPublicNotesForBook(eq(10L), eq(5L), eq(1), eq(20)))
            .thenReturn(page);

        mvc.perform(get("/api/books/10/notes/public")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldCreateReply() throws Exception {
        NoteRequest req = new NoteRequest();
        req.setContent("回复内容");
        req.setType("INSIGHT");

        NoteResponse resp = NoteResponse.builder()
            .id(2L).userId(5L).parentId(1L).rootId(1L)
            .content("回复内容").type("INSIGHT").published(true).build();
        when(noteService.createReply(eq(1L), eq(5L), any())).thenReturn(resp);

        mvc.perform(post("/api/notes/1/reply")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("回复内容"));
    }

    @Test
    void shouldToggleLike() throws Exception {
        when(noteService.toggleLike(eq(1L), eq(5L))).thenReturn(true);

        mvc.perform(post("/api/notes/1/like")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }
}
