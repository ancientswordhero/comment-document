package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.library.config.JwtUtil;
import com.library.config.JwtFilter;
import com.library.config.SecurityConfig;
import com.library.config.WebConfig;
import com.library.dto.*;
import com.library.entity.User;
import com.library.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminBookController.class)
@Import({JwtUtil.class, JwtFilter.class, SecurityConfig.class, WebConfig.class})
class AdminBookControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BookService bookService;

    private String adminToken;

    @BeforeEach
    void setUp() {
        User adminUser = User.builder().id(1L).username("admin").role("ADMIN").build();
        adminToken = new JwtUtil().generateToken(adminUser);
    }

    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + adminToken);
    }

    @Test
    void shouldListAllBooks() throws Exception {
        PageResult<BookResponse> page = PageResult.<BookResponse>builder()
            .records(List.of()).total(0).page(1).size(20).build();
        when(bookService.getAdminBooks(isNull(), isNull(), isNull(), eq(1), eq(20)))
            .thenReturn(page);

        mvc.perform(withAuth(get("/api/admin/books")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldCreateBook() throws Exception {
        BookResponse resp = BookResponse.builder().id(1L).title("新书").build();
        when(bookService.createBook(any(), any(), any(BookRequest.class))).thenReturn(resp);

        MockMultipartFile epubFile = new MockMultipartFile("file", "test.epub",
                "application/epub+zip", "test".getBytes());

        mvc.perform(withAuth(multipart("/api/admin/books")
                .file(epubFile)
                .param("title", "新书")
                .param("author", "作者")
                .param("isbn", "978-0")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("新书"));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        BookResponse resp = BookResponse.builder().id(1L).title("更新").build();
        when(bookService.updateBook(eq(1L), any(), any(), any(BookRequest.class))).thenReturn(resp);

        MockMultipartFile epubFile = new MockMultipartFile("file", "test.epub",
                "application/epub+zip", "test".getBytes());

        mvc.perform(withAuth(multipart("/api/admin/books/1")
                .file(epubFile)
                .param("title", "更新")
                .param("author", "作者")
                .param("isbn", "978-0")
                .with(request -> { request.setMethod("PUT"); return request; })))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("更新"));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        mvc.perform(withAuth(delete("/api/admin/books/1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldToggleStatus() throws Exception {
        BookResponse resp = BookResponse.builder().id(1L).status(0).build();
        when(bookService.toggleStatus(1L)).thenReturn(resp);

        mvc.perform(withAuth(put("/api/admin/books/1/status")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value(0));
    }

}