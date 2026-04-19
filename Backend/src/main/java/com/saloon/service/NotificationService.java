package com.saloon.service;

import com.saloon.dto.response.NotificationResponse;
import com.saloon.entity.Booking;
import com.saloon.entity.Notification;
import com.saloon.entity.User;
import com.saloon.entity.enums.NotificationType;
import com.saloon.exception.ResourceNotFoundException;
import com.saloon.repository.NotificationRepository;
import com.saloon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public void notifyBookingCreated(Booking booking) {
        // Notify customer
        saveNotification(booking.getCustomer(),
            "Booking Submitted",
            "Your booking #" + booking.getBookingReference() + " at " + booking.getSalon().getName() + " is pending approval.",
            NotificationType.BOOKING_CREATED,
            String.valueOf(booking.getId()));
        // Notify owner
        saveNotification(booking.getSalon().getOwner(),
            "New Booking Request",
            "New booking #" + booking.getBookingReference() + " from " + booking.getCustomer().getName(),
            NotificationType.BOOKING_CREATED,
            String.valueOf(booking.getId()));
        sendEmailAsync(booking.getCustomer().getEmail(), "Booking Confirmation",
            "Your booking #" + booking.getBookingReference() + " is pending. We'll notify you once approved.");
    }

    @Transactional
    public void notifyBookingApproved(Booking booking) {
        saveNotification(booking.getCustomer(),
            "Booking Approved! ✅",
            "Your booking #" + booking.getBookingReference() + " at " + booking.getSalon().getName() + " has been approved.",
            NotificationType.BOOKING_APPROVED,
            String.valueOf(booking.getId()));
        sendEmailAsync(booking.getCustomer().getEmail(), "Booking Approved",
            "Great news! Your booking #" + booking.getBookingReference() + " has been approved.");
    }

    @Transactional
    public void notifyBookingRejected(Booking booking) {
        saveNotification(booking.getCustomer(),
            "Booking Rejected",
            "Your booking #" + booking.getBookingReference() + " was rejected. Reason: " + booking.getCancellationReason(),
            NotificationType.BOOKING_REJECTED,
            String.valueOf(booking.getId()));
    }

    @Transactional
    public void notifyBookingCompleted(Booking booking) {
        saveNotification(booking.getCustomer(),
            "Service Completed ✨",
            "Your appointment at " + booking.getSalon().getName() + " is completed. Please leave a review!",
            NotificationType.BOOKING_COMPLETED,
            String.valueOf(booking.getId()));
    }

    @Transactional
    public void notifyBookingCancelled(Booking booking) {
        saveNotification(booking.getCustomer(),
            "Booking Cancelled",
            "Booking #" + booking.getBookingReference() + " has been cancelled.",
            NotificationType.BOOKING_CANCELLED,
            String.valueOf(booking.getId()));
    }

    public List<NotificationResponse> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(this::mapToResponse).collect(Collectors.toList());
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    private void saveNotification(User user, String title, String message, NotificationType type, String refId) {
        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .message(message)
            .type(type)
            .referenceId(refId)
            .build();
        notificationRepository.save(notification);
    }

    @Async
    public void sendEmailAsync(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .title(n.getTitle())
            .message(n.getMessage())
            .type(n.getType())
            .isRead(n.isRead())
            .referenceId(n.getReferenceId())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
