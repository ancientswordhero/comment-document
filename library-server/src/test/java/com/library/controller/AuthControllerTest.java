package com.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.JwtUtil;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;
import com.library.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({com.library.config.JwtUtil.class, com.library.config.JwtFilter.class, com.library.config.SecurityConfig.class, com.library.config.WebConfig.class, com.library.config.FileUploadConfig.class})
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UserService userService;

    @Test
    void shouldRegister() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("reader1");
        req.setPassword("pass123456");
        LoginResponse resp = new LoginResponse("jwt-token", "reader1", "READER");
        when(userService.register(any(RegisterRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
            .andExpect(jsonPath("$.data.username").value("reader1"))
            .andExpect(jsonPath("$.data.role").value("READER"));
    }

    @Test
    void shouldLogin() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        LoginResponse resp = new LoginResponse("jwt-token-admin", "admin", "ADMIN");
        when(userService.login(any(LoginRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void shouldRejectRegisterWithShortPassword() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("reader1");
        req.setPassword("12");

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldRejectMeWithoutToken() throws Exception {
        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldReturnMeWithValidToken() throws Exception {
        // Generate a real token
        JwtUtil jwtUtil = new JwtUtil();
        User testUser = User.builder().id(10L).username("reader1").role("READER").build();
        String token = jwtUtil.generateToken(testUser);

        LoginResponse resp = new LoginResponse(null, "reader1", "READER");
        when(userService.getCurrentUser(10L)).thenReturn(resp);

        mvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("reader1"));
    }

    @Test
    void shouldCreateAdmin() throws Exception {
        // Generate a real admin token
        JwtUtil jwtUtil = new JwtUtil();
        User adminUser = User.builder().id(1L).username("admin").role("ADMIN").build();
        String adminToken = jwtUtil.generateToken(adminUser);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin2");
        req.setPassword("adminpass");
        LoginResponse resp = new LoginResponse("jwt-admin2", "admin2", "ADMIN");
        when(userService.createAdmin(any(RegisterRequest.class))).thenReturn(resp);

        mvc.perform(post("/api/auth/admin")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }
}
