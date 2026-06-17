package com.library.service;

import com.library.dto.NotificationResponse;
import com.library.dto.PageResult;
import com.library.entity.Notification;
import com.library.repository.NotificationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public PageResult<NotificationResponse> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notification> notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationResponse> records = notifPage.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        return PageResult.<NotificationResponse>builder().records(records).total(notifPage.getTotalElements()).page(page).size(size).build();
    }

    public int getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) { n.setIsRead(1); notificationRepository.save(n); }
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Transactional
    public void createNotification(Long userId, String type, String title, String content, Long bookId, Long reviewId) {
        notificationRepository.save(new Notification(userId, type, title, content, bookId, reviewId));
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId()); r.setType(n.getType()); r.setTitle(n.getTitle());
        r.setContent(n.getContent()); r.setBookId(n.getBookId()); r.setReviewId(n.getReviewId());
        r.setNoteId(n.getNoteId());
        r.setRead(n.getIsRead() == 1); r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
