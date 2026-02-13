package com.example.paymentsystem.repository;

import com.example.paymentsystem.entity.PaymentOrder;
import com.example.paymentsystem.entity.PaymentOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderReference(String orderReference);

    List<PaymentOrder> findByCustomerId(String customerId);

    List<PaymentOrder> findByStatus(PaymentOrderStatus status);

    List<PaymentOrder> findByCustomerIdAndStatus(String customerId, PaymentOrderStatus status);

    @Query("SELECT p FROM PaymentOrder p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<PaymentOrder> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT p FROM PaymentOrder p WHERE p.amount >= :minAmount AND p.amount <= :maxAmount")
    List<PaymentOrder> findOrdersByAmountRange(
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );

    @Query("SELECT p FROM PaymentOrder p WHERE p.status = :status AND p.scheduledAt <= :currentTime")
    List<PaymentOrder> findScheduledOrdersReadyForProcessing(
            @Param("status") PaymentOrderStatus status,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("SELECT COUNT(p) FROM PaymentOrder p WHERE p.customerId = :customerId AND p.status = :status")
    Long countByCustomerIdAndStatus(
            @Param("customerId") String customerId,
            @Param("status") PaymentOrderStatus status
    );

    @Query("SELECT SUM(p.amount) FROM PaymentOrder p WHERE p.customerId = :customerId AND p.status = :status")
    BigDecimal sumAmountByCustomerIdAndStatus(
            @Param("customerId") String customerId,
            @Param("status") PaymentOrderStatus status
    );

    boolean existsByOrderReference(String orderReference);
}
