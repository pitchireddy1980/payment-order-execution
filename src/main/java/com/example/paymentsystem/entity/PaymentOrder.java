package com.example.paymentsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderReference;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentOrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(length = 1000)
    private String description;

    // Beneficiary Information
    @Column(nullable = false)
    private String beneficiaryName;

    @Column(nullable = false)
    private String beneficiaryAccount;

    @Column(nullable = false)
    private String beneficiaryBank;

    @Column
    private String beneficiaryBankCode;

    // Payment Execution
    @OneToMany(mappedBy = "paymentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentExecution> executions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime completedAt;

    // Helper methods
    public void addExecution(PaymentExecution execution) {
        executions.add(execution);
        execution.setPaymentOrder(this);
    }

    public void removeExecution(PaymentExecution execution) {
        executions.remove(execution);
        execution.setPaymentOrder(null);
    }
}
