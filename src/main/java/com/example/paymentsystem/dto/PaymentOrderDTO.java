package com.example.paymentsystem.dto;

import com.example.paymentsystem.entity.PaymentMethod;
import com.example.paymentsystem.entity.PaymentOrderStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderDTO {

    private Long id;

    private String orderReference;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    private PaymentOrderStatus status;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Beneficiary name is required")
    private String beneficiaryName;

    @NotBlank(message = "Beneficiary account is required")
    private String beneficiaryAccount;

    @NotBlank(message = "Beneficiary bank is required")
    private String beneficiaryBank;

    private String beneficiaryBankCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime scheduledAt;

    private LocalDateTime completedAt;
}
