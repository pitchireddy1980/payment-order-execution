package com.example.paymentsystem.dto;

import com.example.paymentsystem.entity.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExecutionDTO {

    private Long id;

    private String executionReference;

    private Long paymentOrderId;

    private String orderReference;

    private ExecutionStatus status;

    private BigDecimal amount;

    private String currency;

    private String gatewayTransactionId;

    private String gatewayProvider;

    private Integer retryAttempt;

    private String errorMessage;

    private String errorCode;

    private String gatewayResponse;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime processedAt;

    private LocalDateTime settledAt;

    private String remarks;
}
