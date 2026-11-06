package com.smartshelfx.repository;

import com.smartshelfx.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PurchaseOrderRepository
 * 
 * Provides database queries for Purchase Orders.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    List<PurchaseOrder> findByStatusOrderByCreatedAtDesc(String status);

    List<PurchaseOrder> findByVendorIdOrderByCreatedAtDesc(Long vendorId);

    boolean existsByOrderNumber(String orderNumber);
}
