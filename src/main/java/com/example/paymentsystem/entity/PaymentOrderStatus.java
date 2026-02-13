package com.example.paymentsystem.entity;

public enum PaymentOrderStatus {
    PENDING,          // Order created, awaiting processing
    SCHEDULED,        // Order scheduled for future execution
    PROCESSING,       // Order is being processed
    COMPLETED,        // Order successfully completed
    FAILED,           // Order failed after all retries
    CANCELLED,        // Order cancelled by user/system
    REFUNDED          // Order refunded
}
