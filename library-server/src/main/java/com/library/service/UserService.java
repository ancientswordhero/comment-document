package com.library.service;

import com.library.config.JwtUtil;
import com.library.dto.*;
import com.library.entity.User;
import com.library.repository.ReviewRepository;
import com.library.repository.UserBookRepository;
import com.library.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserBookRepository userBookRepository;
    private final ReviewRepository reviewRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, UserBookRepository userBookRepository,
                       ReviewRepository reviewRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userBookRepository = userBookRepository;
        this.reviewRepository = reviewRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = User.builder()
            .username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .role("READER")
            .build();
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
            .orElse(null);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    public LoginResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        return new LoginResponse(null, user.getUsername(), user.getRole());
    }

    public LoginResponse createAdmin(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = User.builder()
            .username(req.getUsername())
            .password(passwordEncoder.encode(req.getPassword()))
            .role("ADMIN")
            .build();
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }

    @Transactional
    public void deleteAccount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("用户不存在");
        }
        userBookRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        int totalLikes = reviewRepository.sumLikeCountByUserId(userId);
        UserProfileResponse r = new UserProfileResponse();
        r.setId(user.getId());
        r.setUsername(user.getUsername());
        r.setBio(user.getBio());
        r.setTotalLikes(totalLikes);
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        if (req.getUsername() != null && !req.getUsername().trim().isEmpty()) {
            String newName = req.getUsername().trim();
            if (!newName.equals(user.getUsername()) && userRepository.existsByUsername(newName)) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(newName);
        }
        if (req.getBio() != null) {
            user.setBio(req.getBio().trim().isEmpty() ? null : req.getBio().trim());
        }
        userRepository.save(user);
        return getUserProfile(userId);
    }
}
