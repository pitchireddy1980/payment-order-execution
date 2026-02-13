package com.example.paymentsystem.controller;

import com.example.paymentsystem.dto.ApiResponse;
import com.example.paymentsystem.dto.PaymentOrderDTO;
import com.example.paymentsystem.entity.PaymentOrderStatus;
import com.example.paymentsystem.service.PaymentOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/payment-orders")
@RequiredArgsConstructor
@Tag(name = "Payment Orders", description = "Payment Order Management APIs")
public class PaymentOrderController {

    private final PaymentOrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new payment order")
    public ResponseEntity<ApiResponse<PaymentOrderDTO>> createOrder(
            @Valid @RequestBody PaymentOrderDTO orderDTO) {
        
        PaymentOrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment order created successfully", createdOrder));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment order by ID")
    public ResponseEntity<ApiResponse<PaymentOrderDTO>> getOrderById(@PathVariable Long id) {
        PaymentOrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/reference/{orderReference}")
    @Operation(summary = "Get payment order by reference")
    public ResponseEntity<ApiResponse<PaymentOrderDTO>> getOrderByReference(
            @PathVariable String orderReference) {
        
        PaymentOrderDTO order = orderService.getOrderByReference(orderReference);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping
    @Operation(summary = "Get all payment orders")
    public ResponseEntity<ApiResponse<List<PaymentOrderDTO>>> getAllOrders() {
        List<PaymentOrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get payment orders by customer ID")
    public ResponseEntity<ApiResponse<List<PaymentOrderDTO>>> getOrdersByCustomerId(
            @PathVariable String customerId) {
        
        List<PaymentOrderDTO> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get payment orders by status")
    public ResponseEntity<ApiResponse<List<PaymentOrderDTO>>> getOrdersByStatus(
            @PathVariable PaymentOrderStatus status) {
        
        List<PaymentOrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get payment orders between dates")
    public ResponseEntity<ApiResponse<List<PaymentOrderDTO>>> getOrdersBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<PaymentOrderDTO> orders = orderService.getOrdersBetweenDates(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/amount-range")
    @Operation(summary = "Get payment orders by amount range")
    public ResponseEntity<ApiResponse<List<PaymentOrderDTO>>> getOrdersByAmountRange(
            @RequestParam BigDecimal minAmount,
            @RequestParam BigDecimal maxAmount) {
        
        List<PaymentOrderDTO> orders = orderService.getOrdersByAmountRange(minAmount, maxAmount);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update payment order")
    public ResponseEntity<ApiResponse<PaymentOrderDTO>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody PaymentOrderDTO orderDTO) {
        
        PaymentOrderDTO updatedOrder = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok(ApiResponse.success("Payment order updated successfully", updatedOrder));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update payment order status")
    public ResponseEntity<ApiResponse<PaymentOrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam PaymentOrderStatus status) {
        
        PaymentOrderDTO updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel payment order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Payment order cancelled successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment order")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Payment order deleted successfully", null));
    }

    @GetMapping("/customer/{customerId}/stats")
    @Operation(summary = "Get customer payment statistics")
    public ResponseEntity<ApiResponse<CustomerStats>> getCustomerStats(
            @PathVariable String customerId,
            @RequestParam(required = false) PaymentOrderStatus status) {
        
        Long count = orderService.countOrdersByCustomerAndStatus(customerId, status);
        BigDecimal totalAmount = orderService.getTotalAmountByCustomerAndStatus(customerId, status);
        
        CustomerStats stats = new CustomerStats(customerId, status, count, totalAmount);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // Inner class for customer statistics
    public record CustomerStats(
            String customerId,
            PaymentOrderStatus status,
            Long orderCount,
            BigDecimal totalAmount
    ) {}
}
