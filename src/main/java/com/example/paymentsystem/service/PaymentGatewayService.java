package com.example.paymentsystem.service;

import com.example.paymentsystem.entity.PaymentExecution;
import com.example.paymentsystem.entity.PaymentOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class PaymentGatewayService {

    private final Random random = new Random();

    /**
     * Simulates payment processing through a payment gateway
     * In a real implementation, this would integrate with actual payment providers
     * like Stripe, PayPal, Razorpay, etc.
     */
    public boolean processPayment(PaymentExecution execution, PaymentOrder order) {
        log.info("Processing payment through gateway for execution: {}", execution.getExecutionReference());

        try {
            // Simulate API call delay
            Thread.sleep(1000);

            // Simulate gateway transaction
            String gatewayTransactionId = "GW-" + UUID.randomUUID().toString();
            execution.setGatewayTransactionId(gatewayTransactionId);
            execution.setGatewayProvider("MOCK_GATEWAY");

            // Simulate success/failure (80% success rate)
            boolean success = random.nextInt(100) < 80;

            if (success) {
                execution.setGatewayResponse("Payment processed successfully");
                log.info("Payment successful for execution: {}", execution.getExecutionReference());
            } else {
                execution.setGatewayResponse("Payment declined by gateway");
                execution.setErrorCode("GATEWAY_DECLINED");
                execution.setErrorMessage("Insufficient funds or invalid payment method");
                log.warn("Payment failed for execution: {}", execution.getExecutionReference());
            }

            return success;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
            execution.setGatewayResponse("Processing interrupted");
            execution.setErrorCode("PROCESSING_ERROR");
            execution.setErrorMessage(e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error processing payment", e);
            execution.setGatewayResponse("Gateway error");
            execution.setErrorCode("GATEWAY_ERROR");
            execution.setErrorMessage(e.getMessage());
            return false;
        }
    }

    /**
     * Validate payment details before processing
     */
    public boolean validatePaymentDetails(PaymentOrder order) {
        // Add validation logic
        if (order.getAmount().doubleValue() <= 0) {
            return false;
        }
        if (order.getBeneficiaryAccount() == null || order.getBeneficiaryAccount().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Check payment status from gateway
     */
    public String checkPaymentStatus(String gatewayTransactionId) {
        log.info("Checking payment status for transaction: {}", gatewayTransactionId);
        // In real implementation, call gateway API to check status
        return "COMPLETED";
    }

    /**
     * Initiate refund through gateway
     */
    public boolean initiateRefund(String gatewayTransactionId) {
        log.info("Initiating refund for transaction: {}", gatewayTransactionId);
        // In real implementation, call gateway refund API
        return true;
    }
}
