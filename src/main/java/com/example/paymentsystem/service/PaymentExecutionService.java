package com.example.paymentsystem.service;

import com.example.paymentsystem.dto.PaymentExecutionDTO;
import com.example.paymentsystem.entity.ExecutionStatus;

import java.util.List;

public interface PaymentExecutionService {

    PaymentExecutionDTO executePayment(Long orderId);

    PaymentExecutionDTO getExecutionById(Long id);

    PaymentExecutionDTO getExecutionByReference(String executionReference);

    List<PaymentExecutionDTO> getExecutionsByOrderId(Long orderId);

    List<PaymentExecutionDTO> getExecutionsByOrderReference(String orderReference);

    List<PaymentExecutionDTO> getExecutionsByStatus(ExecutionStatus status);

    List<PaymentExecutionDTO> getExecutionsByCustomerId(String customerId);

    PaymentExecutionDTO updateExecutionStatus(Long id, ExecutionStatus newStatus);

    PaymentExecutionDTO retryExecution(Long executionId);

    void processSettlement(Long executionId);

    void reverseExecution(Long executionId);
}
