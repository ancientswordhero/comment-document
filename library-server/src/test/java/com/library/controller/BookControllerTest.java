package com.library.controller;


import com.library.config.JwtFilter;
import com.library.config.JwtUtil;
import com.library.config.SecurityConfig;
import com.library.config.WebConfig;
import com.library.dto.*;
import com.library.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import({JwtUtil.class, JwtFilter.class, SecurityConfig.class, WebConfig.class})
class BookControllerTest {

    @Autowired MockMvc mvc;
    @MockBean BookService bookService;
    @MockBean CategoryService categoryService;

    @Test
    void shouldReturnBookList() throws Exception {
        BookResponse book = BookResponse.builder().id(1L).title("红楼梦").author("曹雪芹")
            .isbn("978-7-02").categoryName("文学").status(1).build();
        PageResult<BookResponse> page = PageResult.<BookResponse>builder()
            .records(List.of(book)).total(1).page(1).size(20).build();
        when(bookService.getBooks(isNull(), isNull(), eq(1), eq(20))).thenReturn(page);

        mvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].title").value("红楼梦"));
    }

    @Test
    void shouldReturnBookDetail() throws Exception {
        BookResponse book = BookResponse.builder().id(1L).title("三体").author("刘慈欣").build();
        when(bookService.getBookById(1L)).thenReturn(book);

        mvc.perform(get("/api/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("三体"));
    }

    @Test
    void shouldReturnCategoryTree() throws Exception {
        CategoryResponse cat = CategoryResponse.builder().id(1L).name("文学").build();
        when(categoryService.getCategoryTree()).thenReturn(List.of(cat));

        mvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("文学"));
    }

    @Test
    void getCover_shouldReturnCoverImage() throws Exception {
        byte[] coverBytes = "fake-image-data".getBytes();
        when(bookService.getCoverData(1L)).thenReturn(coverBytes);

        mvc.perform(get("/api/books/1/cover"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(coverBytes));
    }

    @Test
    void getCover_shouldReturn404_whenNoCover() throws Exception {
        when(bookService.getCoverData(1L))
            .thenThrow(new EntityNotFoundException("该图书无封面"));

        mvc.perform(get("/api/books/1/cover"))
            .andExpect(status().isNotFound());
    }
}
