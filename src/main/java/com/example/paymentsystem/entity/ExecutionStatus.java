package com.example.paymentsystem.entity;

public enum ExecutionStatus {
    INITIATED,        // Execution started
    PENDING,          // Waiting for gateway response
    PROCESSING,       // Being processed by gateway
    SUCCESS,          // Successfully executed
    FAILED,           // Execution failed
    TIMEOUT,          // Gateway timeout
    SETTLED,          // Payment settled
    REVERSED          // Payment reversed/refunded
}
