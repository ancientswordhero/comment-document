package com.library.service;

import com.library.config.JwtUtil;
import com.library.dto.LoginRequest;
import com.library.dto.LoginResponse;
import com.library.dto.RegisterRequest;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtUtil jwtUtil;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @InjectMocks UserService userService;

    @Test
    void shouldRegisterReader() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("reader1");
        req.setPassword("pass123456");
        when(userRepository.existsByUsername("reader1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token-xyz");

        LoginResponse resp = userService.register(req);

        assertThat(resp.getUsername()).isEqualTo("reader1");
        assertThat(resp.getRole()).isEqualTo("READER");
        assertThat(resp.getToken()).isEqualTo("jwt-token-xyz");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("reader1");
        req.setPassword("pass123456");
        when(userRepository.existsByUsername("reader1")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("用户名已存在");
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("admin123");
        String encodedPassword = passwordEncoder.encode("admin123");
        User user = User.builder().id(1L).username("admin")
            .password(encodedPassword).role("ADMIN").build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token-admin");

        LoginResponse resp = userService.login(req);

        assertThat(resp.getUsername()).isEqualTo("admin");
        assertThat(resp.getRole()).isEqualTo("ADMIN");
        assertThat(resp.getToken()).isEqualTo("jwt-token-admin");
    }

    @Test
    void shouldRejectWrongPassword() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrongpass");
        String encodedPassword = passwordEncoder.encode("admin123");
        User user = User.builder().id(1L).username("admin")
            .password(encodedPassword).role("ADMIN").build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void shouldRejectNonexistentUser() {
        LoginRequest req = new LoginRequest();
        req.setUsername("nobody");
        req.setPassword("whatever");
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void shouldGetCurrentUser() {
        User user = User.builder().id(1L).username("reader1")
            .role("READER").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        LoginResponse resp = userService.getCurrentUser(1L);

        assertThat(resp.getUsername()).isEqualTo("reader1");
        assertThat(resp.getRole()).isEqualTo("READER");
    }

    @Test
    void shouldCreateAdmin() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("admin2");
        req.setPassword("adminpass");
        when(userRepository.existsByUsername("admin2")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token-admin2");

        LoginResponse resp = userService.createAdmin(req);

        assertThat(resp.getRole()).isEqualTo("ADMIN");
        verify(userRepository).save(any(User.class));
    }
}
