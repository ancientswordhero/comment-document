package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.library.config.JwtFilter;
import com.library.config.JwtUtil;
import com.library.config.SecurityConfig;
import com.library.config.WebConfig;
import com.library.dto.*;
import com.library.entity.User;
import com.library.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@Import({JwtUtil.class, JwtFilter.class, SecurityConfig.class, WebConfig.class})
class ReviewControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtUtil jwtUtil;
    @MockBean ReviewService reviewService;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        User testUser = User.builder().id(5L).username("testuser").role("USER").build();
        User testAdmin = User.builder().id(1L).username("admin").role("ADMIN").build();
        userToken = jwtUtil.generateToken(testUser);
        adminToken = jwtUtil.generateToken(testAdmin);
    }

    @Test
    void shouldListReviews() throws Exception {
        ReviewResponse review = ReviewResponse.builder().id(1L).bookId(10L)
            .content("好").username("小明").likeCount(3).replyCount(0)
            .replies(List.of()).build();
        PageResult<ReviewResponse> page = PageResult.<ReviewResponse>builder()
            .records(List.of(review)).total(1).page(1).size(10).build();
        when(reviewService.getReviews(eq(10L), eq("time"), eq(1), eq(10), isNull()))
            .thenReturn(page);

        mvc.perform(get("/api/books/10/reviews"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].username").value("小明"));
    }

    @Test
    void shouldCreateReview() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setContent("好书！");
        ReviewResponse resp = ReviewResponse.builder().id(1L).content("好书！").build();
        when(reviewService.createReview(eq(10L), eq(5L), any())).thenReturn(resp);

        mvc.perform(post("/api/books/10/reviews")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("好书！"));
    }

    @Test
    void shouldCreateReply() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setContent("回复");
        ReviewResponse resp = ReviewResponse.builder().id(2L).content("回复").parentId(1L).build();
        when(reviewService.createReply(eq(1L), eq(5L), any())).thenReturn(resp);

        mvc.perform(post("/api/reviews/1/reply")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("回复"));
    }

    @Test
    void shouldUpdateReview() throws Exception {
        ReviewRequest req = new ReviewRequest();
        req.setContent("修改后");
        ReviewResponse resp = ReviewResponse.builder().id(1L).content("修改后").build();
        when(reviewService.updateReview(eq(1L), eq(5L), eq(false), any())).thenReturn(resp);

        mvc.perform(put("/api/reviews/1")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("修改后"));
    }

    @Test
    void shouldDeleteReview() throws Exception {
        mvc.perform(delete("/api/reviews/1")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldToggleLike() throws Exception {
        when(reviewService.toggleLike(1L, 5L)).thenReturn(true);

        mvc.perform(post("/api/reviews/1/like")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
    }
}
