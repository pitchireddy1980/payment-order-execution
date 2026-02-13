package com.example.paymentsystem.repository;

import com.example.paymentsystem.entity.ExecutionStatus;
import com.example.paymentsystem.entity.PaymentExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentExecutionRepository extends JpaRepository<PaymentExecution, Long> {

    Optional<PaymentExecution> findByExecutionReference(String executionReference);

    List<PaymentExecution> findByPaymentOrderId(Long paymentOrderId);

    List<PaymentExecution> findByStatus(ExecutionStatus status);

    Optional<PaymentExecution> findByGatewayTransactionId(String gatewayTransactionId);

    @Query("SELECT e FROM PaymentExecution e WHERE e.paymentOrder.id = :orderId ORDER BY e.createdAt DESC")
    List<PaymentExecution> findExecutionsByOrderIdOrderByCreatedAtDesc(@Param("orderId") Long orderId);

    @Query("SELECT e FROM PaymentExecution e WHERE e.paymentOrder.orderReference = :orderReference")
    List<PaymentExecution> findByOrderReference(@Param("orderReference") String orderReference);

    @Query("SELECT e FROM PaymentExecution e WHERE e.status = :status AND e.createdAt < :threshold")
    List<PaymentExecution> findStaleExecutions(
            @Param("status") ExecutionStatus status,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("SELECT COUNT(e) FROM PaymentExecution e WHERE e.paymentOrder.id = :orderId AND e.status = :status")
    Long countByPaymentOrderIdAndStatus(
            @Param("orderId") Long orderId,
            @Param("status") ExecutionStatus status
    );

    @Query("SELECT e FROM PaymentExecution e WHERE e.paymentOrder.customerId = :customerId ORDER BY e.createdAt DESC")
    List<PaymentExecution> findByCustomerId(@Param("customerId") String customerId);

    boolean existsByExecutionReference(String executionReference);
}
