package com.smartshelfx.service;

import com.smartshelfx.dto.NotificationResponse;
import com.smartshelfx.entity.Notification;
import com.smartshelfx.entity.Product;
import com.smartshelfx.exception.ResourceNotFoundException;
import com.smartshelfx.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NotificationService
 * 
 * Manages database-backed system alerts for warehouse operations.
 * Automatically generates low-stock and out-of-stock notifications
 * while preventing spam duplicate unread alerts.
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications() {
        return notificationRepository.findByReadFalseOrderByCreatedAtDesc().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByReadFalseOrderByCreatedAtDesc();
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    public Notification createNotification(String title, String message, String type, Long productId) {
        Notification notification = new Notification(title, message, type, productId);
        return notificationRepository.save(notification);
    }

    /**
     * Checks a product's stock balance and fires an alert if below threshold,
     * suppressing duplicates if an unread alert already exists for this product.
     */
    public void checkAndTriggerStockAlert(Product product) {
        if (product.getCurrentStock() <= 0) {
            if (!notificationRepository.existsByProductIdAndTypeAndReadFalse(product.getId(), "OUT_OF_STOCK")) {
                createNotification(
                        "Out of Stock: " + product.getName(),
                        String.format("Product '%s' (SKU: %s) has reached 0 units. Replenishment is urgently needed.",
                                product.getName(), product.getSku()),
                        "OUT_OF_STOCK",
                        product.getId()
                );
            }
        } else if (product.getCurrentStock() <= product.getMinStockThreshold()) {
            if (!notificationRepository.existsByProductIdAndTypeAndReadFalse(product.getId(), "LOW_STOCK")) {
                createNotification(
                        "Low Stock Warning: " + product.getName(),
                        String.format("Product '%s' (SKU: %s) is low on stock (%d units remaining, threshold is %d).",
                                product.getName(), product.getSku(), product.getCurrentStock(), product.getMinStockThreshold()),
                        "LOW_STOCK",
                        product.getId()
                );
            }
        }
    }
}
