package com.example.paymentsystem.service.impl;

import com.example.paymentsystem.dto.PaymentExecutionDTO;
import com.example.paymentsystem.entity.*;
import com.example.paymentsystem.exception.InvalidOperationException;
import com.example.paymentsystem.exception.ResourceNotFoundException;
import com.example.paymentsystem.repository.PaymentExecutionRepository;
import com.example.paymentsystem.repository.PaymentOrderRepository;
import com.example.paymentsystem.service.PaymentExecutionService;
import com.example.paymentsystem.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentExecutionServiceImpl implements PaymentExecutionService {

    private final PaymentExecutionRepository executionRepository;
    private final PaymentOrderRepository orderRepository;
    private final PaymentGatewayService gatewayService;
    private final ModelMapper modelMapper;

    @Override
    public PaymentExecutionDTO executePayment(Long orderId) {
        log.info("Executing payment for order ID: {}", orderId);

        PaymentOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found with ID: " + orderId));

        validateOrderForExecution(order);

        // Create execution record
        PaymentExecution execution = new PaymentExecution();
        execution.setExecutionReference(generateExecutionReference());
        execution.setPaymentOrder(order);
        execution.setAmount(order.getAmount());
        execution.setCurrency(order.getCurrency());
        execution.setStatus(ExecutionStatus.INITIATED);
        execution.setRetryAttempt(0);
        execution.setCreatedAt(LocalDateTime.now());
        execution.setUpdatedAt(LocalDateTime.now());

        // Update order status
        order.setStatus(PaymentOrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());

        PaymentExecution savedExecution = executionRepository.save(execution);

        // Process payment through gateway
        try {
            processPaymentThroughGateway(savedExecution, order);
        } catch (Exception e) {
            log.error("Payment execution failed: {}", e.getMessage(), e);
            handleExecutionFailure(savedExecution, e.getMessage());
        }

        return convertToDTO(savedExecution);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentExecutionDTO getExecutionById(Long id) {
        log.info("Fetching payment execution with ID: {}", id);

        PaymentExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment execution not found with ID: " + id));

        return convertToDTO(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentExecutionDTO getExecutionByReference(String executionReference) {
        log.info("Fetching payment execution with reference: {}", executionReference);

        PaymentExecution execution = executionRepository.findByExecutionReference(executionReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment execution not found with reference: " + executionReference));

        return convertToDTO(execution);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentExecutionDTO> getExecutionsByOrderId(Long orderId) {
        log.info("Fetching payment executions for order ID: {}", orderId);

        return executionRepository.findExecutionsByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentExecutionDTO> getExecutionsByOrderReference(String orderReference) {
        log.info("Fetching payment executions for order reference: {}", orderReference);

        return executionRepository.findByOrderReference(orderReference).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentExecutionDTO> getExecutionsByStatus(ExecutionStatus status) {
        log.info("Fetching payment executions with status: {}", status);

        return executionRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentExecutionDTO> getExecutionsByCustomerId(String customerId) {
        log.info("Fetching payment executions for customer: {}", customerId);

        return executionRepository.findByCustomerId(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentExecutionDTO updateExecutionStatus(Long id, ExecutionStatus newStatus) {
        log.info("Updating execution {} status to {}", id, newStatus);

        PaymentExecution execution = executionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment execution not found with ID: " + id));

        execution.setStatus(newStatus);
        execution.setUpdatedAt(LocalDateTime.now());

        if (newStatus == ExecutionStatus.SUCCESS) {
            execution.setProcessedAt(LocalDateTime.now());
            updateOrderStatusOnSuccess(execution.getPaymentOrder());
        } else if (newStatus == ExecutionStatus.FAILED) {
            updateOrderStatusOnFailure(execution.getPaymentOrder());
        }

        PaymentExecution updatedExecution = executionRepository.save(execution);
        return convertToDTO(updatedExecution);
    }

    @Override
    public PaymentExecutionDTO retryExecution(Long executionId) {
        log.info("Retrying payment execution with ID: {}", executionId);

        PaymentExecution originalExecution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment execution not found with ID: " + executionId));

        if (originalExecution.getStatus() != ExecutionStatus.FAILED) {
            throw new InvalidOperationException("Can only retry failed executions");
        }

        PaymentOrder order = originalExecution.getPaymentOrder();

        // Create new execution for retry
        PaymentExecution retryExecution = new PaymentExecution();
        retryExecution.setExecutionReference(generateExecutionReference());
        retryExecution.setPaymentOrder(order);
        retryExecution.setAmount(order.getAmount());
        retryExecution.setCurrency(order.getCurrency());
        retryExecution.setStatus(ExecutionStatus.INITIATED);
        retryExecution.setRetryAttempt(originalExecution.getRetryAttempt() + 1);
        retryExecution.setCreatedAt(LocalDateTime.now());
        retryExecution.setUpdatedAt(LocalDateTime.now());

        PaymentExecution savedExecution = executionRepository.save(retryExecution);

        try {
            processPaymentThroughGateway(savedExecution, order);
        } catch (Exception e) {
            log.error("Payment retry failed: {}", e.getMessage(), e);
            handleExecutionFailure(savedExecution, e.getMessage());
        }

        return convertToDTO(savedExecution);
    }

    @Override
    public void processSettlement(Long executionId) {
        log.info("Processing settlement for execution ID: {}", executionId);

        PaymentExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment execution not found with ID: " + executionId));

        if (execution.getStatus() != ExecutionStatus.SUCCESS) {
            throw new InvalidOperationException("Can only settle successful executions");
        }

        execution.setStatus(ExecutionStatus.SETTLED);
        execution.setSettledAt(LocalDateTime.now());
        execution.setUpdatedAt(LocalDateTime.now());

        executionRepository.save(execution);
    }

    @Override
    public void reverseExecution(Long executionId) {
        log.info("Reversing payment execution with ID: {}", executionId);

        PaymentExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment execution not found with ID: " + executionId));

        if (execution.getStatus() != ExecutionStatus.SUCCESS && 
            execution.getStatus() != ExecutionStatus.SETTLED) {
            throw new InvalidOperationException(
                    "Can only reverse successful or settled executions");
        }

        execution.setStatus(ExecutionStatus.REVERSED);
        execution.setUpdatedAt(LocalDateTime.now());

        PaymentOrder order = execution.getPaymentOrder();
        order.setStatus(PaymentOrderStatus.REFUNDED);
        order.setUpdatedAt(LocalDateTime.now());

        executionRepository.save(execution);
        orderRepository.save(order);
    }

    private void processPaymentThroughGateway(PaymentExecution execution, PaymentOrder order) {
        execution.setStatus(ExecutionStatus.PROCESSING);
        execution.setUpdatedAt(LocalDateTime.now());
        executionRepository.save(execution);

        // Call payment gateway service
        boolean success = gatewayService.processPayment(execution, order);

        if (success) {
            execution.setStatus(ExecutionStatus.SUCCESS);
            execution.setProcessedAt(LocalDateTime.now());
            updateOrderStatusOnSuccess(order);
        } else {
            execution.setStatus(ExecutionStatus.FAILED);
            updateOrderStatusOnFailure(order);
        }

        execution.setUpdatedAt(LocalDateTime.now());
        executionRepository.save(execution);
    }

    private void handleExecutionFailure(PaymentExecution execution, String errorMessage) {
        execution.setStatus(ExecutionStatus.FAILED);
        execution.setErrorMessage(errorMessage);
        execution.setUpdatedAt(LocalDateTime.now());
        executionRepository.save(execution);

        updateOrderStatusOnFailure(execution.getPaymentOrder());
    }

    private void updateOrderStatusOnSuccess(PaymentOrder order) {
        order.setStatus(PaymentOrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void updateOrderStatusOnFailure(PaymentOrder order) {
        order.setStatus(PaymentOrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private void validateOrderForExecution(PaymentOrder order) {
        if (order.getStatus() == PaymentOrderStatus.COMPLETED) {
            throw new InvalidOperationException("Order is already completed");
        }
        if (order.getStatus() == PaymentOrderStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot execute cancelled order");
        }
    }

    private String generateExecutionReference() {
        return "EXE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentExecutionDTO convertToDTO(PaymentExecution execution) {
        PaymentExecutionDTO dto = modelMapper.map(execution, PaymentExecutionDTO.class);
        dto.setPaymentOrderId(execution.getPaymentOrder().getId());
        dto.setOrderReference(execution.getPaymentOrder().getOrderReference());
        return dto;
    }
}
