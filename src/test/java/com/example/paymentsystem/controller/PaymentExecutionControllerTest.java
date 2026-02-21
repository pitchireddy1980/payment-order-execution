package com.example.paymentsystem.controller;

import com.example.paymentsystem.dto.PaymentExecutionDTO;
import com.example.paymentsystem.entity.ExecutionStatus;
import com.example.paymentsystem.exception.InvalidOperationException;
import com.example.paymentsystem.exception.ResourceNotFoundException;
import com.example.paymentsystem.service.PaymentExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentExecutionController.class)
class PaymentExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentExecutionService executionService;

    private PaymentExecutionDTO executionDTO;

    @BeforeEach
    void setUp() {
        executionDTO = new PaymentExecutionDTO();
        executionDTO.setId(1L);
        executionDTO.setExecutionReference("EXE-TEST123");
        executionDTO.setPaymentOrderId(1L);
        executionDTO.setOrderReference("ORD-TEST123");
        executionDTO.setStatus(ExecutionStatus.SUCCESS);
        executionDTO.setAmount(new BigDecimal("100.00"));
        executionDTO.setCurrency("USD");
        executionDTO.setRetryAttempt(0);
    }

    @Nested
    @DisplayName("Execute Payment")
    class ExecutePayment {
        @Test
        void executePayment_Returns201() throws Exception {
            when(executionService.executePayment(1L)).thenReturn(executionDTO);

            mockMvc.perform(post("/v1/payment-executions/execute/1"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.executionReference").value("EXE-TEST123"))
                    .andExpect(jsonPath("$.data.paymentOrderId").value(1));

            verify(executionService).executePayment(1L);
        }

        @Test
        void executePayment_OrderNotFound_Returns404() throws Exception {
            when(executionService.executePayment(999L))
                    .thenThrow(new ResourceNotFoundException("Payment order not found with ID: 999"));

            mockMvc.perform(post("/v1/payment-executions/execute/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get Execution by ID")
    class GetExecutionById {
        @Test
        void getExecutionById_Returns200() throws Exception {
            when(executionService.getExecutionById(1L)).thenReturn(executionDTO);

            mockMvc.perform(get("/v1/payment-executions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("SUCCESS"));
        }

        @Test
        void getExecutionById_NotFound_Returns404() throws Exception {
            when(executionService.getExecutionById(999L))
                    .thenThrow(new ResourceNotFoundException("Payment execution not found with ID: 999"));

            mockMvc.perform(get("/v1/payment-executions/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get Execution by Reference")
    class GetExecutionByReference {
        @Test
        void getExecutionByReference_Returns200() throws Exception {
            when(executionService.getExecutionByReference("EXE-TEST123")).thenReturn(executionDTO);

            mockMvc.perform(get("/v1/payment-executions/reference/EXE-TEST123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.executionReference").value("EXE-TEST123"));
        }
    }

    @Nested
    @DisplayName("Get Executions by Order ID")
    class GetExecutionsByOrderId {
        @Test
        void getExecutionsByOrderId_Returns200() throws Exception {
            when(executionService.getExecutionsByOrderId(1L)).thenReturn(List.of(executionDTO));

            mockMvc.perform(get("/v1/payment-executions/order/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("Get Executions by Order Reference")
    class GetExecutionsByOrderReference {
        @Test
        void getExecutionsByOrderReference_Returns200() throws Exception {
            when(executionService.getExecutionsByOrderReference("ORD-TEST123"))
                    .thenReturn(List.of(executionDTO));

            mockMvc.perform(get("/v1/payment-executions/order-reference/ORD-TEST123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].orderReference").value("ORD-TEST123"));
        }
    }

    @Nested
    @DisplayName("Get Executions by Status")
    class GetExecutionsByStatus {
        @Test
        void getExecutionsByStatus_Returns200() throws Exception {
            when(executionService.getExecutionsByStatus(ExecutionStatus.SUCCESS))
                    .thenReturn(List.of(executionDTO));

            mockMvc.perform(get("/v1/payment-executions/status/SUCCESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("SUCCESS"));
        }
    }

    @Nested
    @DisplayName("Get Executions by Customer ID")
    class GetExecutionsByCustomerId {
        @Test
        void getExecutionsByCustomerId_Returns200() throws Exception {
            when(executionService.getExecutionsByCustomerId("CUST001"))
                    .thenReturn(List.of(executionDTO));

            mockMvc.perform(get("/v1/payment-executions/customer/CUST001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("Update Execution Status")
    class UpdateExecutionStatus {
        @Test
        void updateExecutionStatus_Returns200() throws Exception {
            when(executionService.updateExecutionStatus(1L, ExecutionStatus.SETTLED))
                    .thenReturn(executionDTO);

            mockMvc.perform(patch("/v1/payment-executions/1/status")
                            .param("status", "SETTLED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Execution status updated"));
        }
    }

    @Nested
    @DisplayName("Retry Execution")
    class RetryExecution {
        @Test
        void retryExecution_Returns200() throws Exception {
            when(executionService.retryExecution(1L)).thenReturn(executionDTO);

            mockMvc.perform(post("/v1/payment-executions/1/retry"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment execution retried"));
        }

        @Test
        void retryExecution_InvalidOperation_Returns400() throws Exception {
            when(executionService.retryExecution(1L))
                    .thenThrow(new InvalidOperationException("Can only retry failed executions"));

            mockMvc.perform(post("/v1/payment-executions/1/retry"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Process Settlement")
    class ProcessSettlement {
        @Test
        void processSettlement_Returns200() throws Exception {
            doNothing().when(executionService).processSettlement(1L);

            mockMvc.perform(post("/v1/payment-executions/1/settle"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Settlement processed successfully"));

            verify(executionService).processSettlement(1L);
        }
    }

    @Nested
    @DisplayName("Reverse Execution")
    class ReverseExecution {
        @Test
        void reverseExecution_Returns200() throws Exception {
            doNothing().when(executionService).reverseExecution(1L);

            mockMvc.perform(post("/v1/payment-executions/1/reverse"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment execution reversed"));

            verify(executionService).reverseExecution(1L);
        }
    }
}
