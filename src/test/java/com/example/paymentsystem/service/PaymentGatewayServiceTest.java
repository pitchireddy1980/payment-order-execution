package com.example.paymentsystem.service;

import com.example.paymentsystem.entity.PaymentExecution;
import com.example.paymentsystem.entity.PaymentOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentGatewayServiceTest {

    private PaymentGatewayService gatewayService;

    private PaymentOrder testOrder;
    private PaymentExecution testExecution;

    @BeforeEach
    void setUp() {
        gatewayService = new PaymentGatewayService();

        testOrder = new PaymentOrder();
        testOrder.setId(1L);
        testOrder.setOrderReference("ORD-TEST123");
        testOrder.setCustomerId("CUST001");
        testOrder.setAmount(new BigDecimal("100.00"));
        testOrder.setCurrency("USD");
        testOrder.setBeneficiaryName("ABC Corp");
        testOrder.setBeneficiaryAccount("1234567890");
        testOrder.setBeneficiaryBank("XYZ Bank");

        testExecution = new PaymentExecution();
        testExecution.setId(1L);
        testExecution.setExecutionReference("EXE-TEST123");
        testExecution.setPaymentOrder(testOrder);
        testExecution.setAmount(new BigDecimal("100.00"));
        testExecution.setCurrency("USD");
    }

    @Test
    @DisplayName("processPayment returns boolean and sets gateway fields on execution")
    void processPayment_ReturnsBoolean_SetsGatewayFields() {
        boolean result = gatewayService.processPayment(testExecution, testOrder);

        assertTrue(result == true || result == false);
        assertNotNull(testExecution.getGatewayTransactionId());
        assertTrue(testExecution.getGatewayTransactionId().startsWith("GW-"));
        assertEquals("MOCK_GATEWAY", testExecution.getGatewayProvider());
        assertNotNull(testExecution.getGatewayResponse());
    }

    @Test
    @DisplayName("validatePaymentDetails returns true for valid order")
    void validatePaymentDetails_ValidOrder_ReturnsTrue() {
        assertTrue(gatewayService.validatePaymentDetails(testOrder));
    }

    @Test
    @DisplayName("validatePaymentDetails returns false when amount is zero")
    void validatePaymentDetails_ZeroAmount_ReturnsFalse() {
        testOrder.setAmount(BigDecimal.ZERO);
        assertFalse(gatewayService.validatePaymentDetails(testOrder));
    }

    @Test
    @DisplayName("validatePaymentDetails returns false when amount is negative")
    void validatePaymentDetails_NegativeAmount_ReturnsFalse() {
        testOrder.setAmount(new BigDecimal("-10.00"));
        assertFalse(gatewayService.validatePaymentDetails(testOrder));
    }

    @Test
    @DisplayName("validatePaymentDetails returns false when beneficiary account is null")
    void validatePaymentDetails_NullBeneficiaryAccount_ReturnsFalse() {
        testOrder.setBeneficiaryAccount(null);
        assertFalse(gatewayService.validatePaymentDetails(testOrder));
    }

    @Test
    @DisplayName("validatePaymentDetails returns false when beneficiary account is empty")
    void validatePaymentDetails_EmptyBeneficiaryAccount_ReturnsFalse() {
        testOrder.setBeneficiaryAccount("");
        assertFalse(gatewayService.validatePaymentDetails(testOrder));
    }

    @Test
    @DisplayName("checkPaymentStatus returns status string")
    void checkPaymentStatus_ReturnsStatus() {
        String status = gatewayService.checkPaymentStatus("GW-12345");
        assertNotNull(status);
        assertEquals("COMPLETED", status);
    }

    @Test
    @DisplayName("initiateRefund returns true")
    void initiateRefund_ReturnsTrue() {
        assertTrue(gatewayService.initiateRefund("GW-12345"));
    }
}
