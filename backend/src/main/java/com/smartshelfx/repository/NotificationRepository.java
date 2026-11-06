package com.smartshelfx.repository;

import com.smartshelfx.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NotificationRepository
 * 
 * Provides database queries for system alerts.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByReadFalseOrderByCreatedAtDesc();

    long countByReadFalse();

    boolean existsByProductIdAndTypeAndReadFalse(Long productId, String type);
}
