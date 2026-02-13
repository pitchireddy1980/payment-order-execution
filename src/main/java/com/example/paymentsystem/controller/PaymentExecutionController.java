package com.example.paymentsystem.controller;

import com.example.paymentsystem.dto.ApiResponse;
import com.example.paymentsystem.dto.PaymentExecutionDTO;
import com.example.paymentsystem.entity.ExecutionStatus;
import com.example.paymentsystem.service.PaymentExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payment-executions")
@RequiredArgsConstructor
@Tag(name = "Payment Executions", description = "Payment Execution Management APIs")
public class PaymentExecutionController {

    private final PaymentExecutionService executionService;

    @PostMapping("/execute/{orderId}")
    @Operation(summary = "Execute payment for an order")
    public ResponseEntity<ApiResponse<PaymentExecutionDTO>> executePayment(@PathVariable Long orderId) {
        PaymentExecutionDTO execution = executionService.executePayment(orderId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment execution initiated", execution));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment execution by ID")
    public ResponseEntity<ApiResponse<PaymentExecutionDTO>> getExecutionById(@PathVariable Long id) {
        PaymentExecutionDTO execution = executionService.getExecutionById(id);
        return ResponseEntity.ok(ApiResponse.success(execution));
    }

    @GetMapping("/reference/{executionReference}")
    @Operation(summary = "Get payment execution by reference")
    public ResponseEntity<ApiResponse<PaymentExecutionDTO>> getExecutionByReference(
            @PathVariable String executionReference) {
        
        PaymentExecutionDTO execution = executionService.getExecutionByReference(executionReference);
        return ResponseEntity.ok(ApiResponse.success(execution));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get all executions for an order")
    public ResponseEntity<ApiResponse<List<PaymentExecutionDTO>>> getExecutionsByOrderId(
            @PathVariable Long orderId) {
        
        List<PaymentExecutionDTO> executions = executionService.getExecutionsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    @GetMapping("/order-reference/{orderReference}")
    @Operation(summary = "Get all executions for an order by reference")
    public ResponseEntity<ApiResponse<List<PaymentExecutionDTO>>> getExecutionsByOrderReference(
            @PathVariable String orderReference) {
        
        List<PaymentExecutionDTO> executions = executionService.getExecutionsByOrderReference(orderReference);
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get executions by status")
    public ResponseEntity<ApiResponse<List<PaymentExecutionDTO>>> getExecutionsByStatus(
            @PathVariable ExecutionStatus status) {
        
        List<PaymentExecutionDTO> executions = executionService.getExecutionsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get executions by customer ID")
    public ResponseEntity<ApiResponse<List<PaymentExecutionDTO>>> getExecutionsByCustomerId(
            @PathVariable String customerId) {
        
        List<PaymentExecutionDTO> executions = executionService.getExecutionsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(executions));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update execution status")
    public ResponseEntity<ApiResponse<PaymentExecutionDTO>> updateExecutionStatus(
            @PathVariable Long id,
            @RequestParam ExecutionStatus status) {
        
        PaymentExecutionDTO execution = executionService.updateExecutionStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Execution status updated", execution));
    }

    @PostMapping("/{id}/retry")
    @Operation(summary = "Retry failed execution")
    public ResponseEntity<ApiResponse<PaymentExecutionDTO>> retryExecution(@PathVariable Long id) {
        PaymentExecutionDTO execution = executionService.retryExecution(id);
        return ResponseEntity.ok(ApiResponse.success("Payment execution retried", execution));
    }

    @PostMapping("/{id}/settle")
    @Operation(summary = "Process settlement for execution")
    public ResponseEntity<ApiResponse<Void>> processSettlement(@PathVariable Long id) {
        executionService.processSettlement(id);
        return ResponseEntity.ok(ApiResponse.success("Settlement processed successfully", null));
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse payment execution")
    public ResponseEntity<ApiResponse<Void>> reverseExecution(@PathVariable Long id) {
        executionService.reverseExecution(id);
        return ResponseEntity.ok(ApiResponse.success("Payment execution reversed", null));
    }
}
