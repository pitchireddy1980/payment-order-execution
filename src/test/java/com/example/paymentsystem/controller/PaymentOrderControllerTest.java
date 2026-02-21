package com.example.paymentsystem.controller;

import com.example.paymentsystem.dto.PaymentOrderDTO;
import com.example.paymentsystem.entity.PaymentMethod;
import com.example.paymentsystem.entity.PaymentOrderStatus;
import com.example.paymentsystem.exception.InvalidOperationException;
import com.example.paymentsystem.exception.ResourceNotFoundException;
import com.example.paymentsystem.service.PaymentOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentOrderController.class)
class PaymentOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentOrderService orderService;

    private ObjectMapper objectMapper;
    private PaymentOrderDTO validOrderDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validOrderDTO = new PaymentOrderDTO();
        validOrderDTO.setId(1L);
        validOrderDTO.setOrderReference("ORD-TEST123");
        validOrderDTO.setCustomerId("CUST001");
        validOrderDTO.setCustomerName("John Doe");
        validOrderDTO.setCustomerEmail("john@example.com");
        validOrderDTO.setAmount(new BigDecimal("100.00"));
        validOrderDTO.setCurrency("USD");
        validOrderDTO.setStatus(PaymentOrderStatus.PENDING);
        validOrderDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        validOrderDTO.setBeneficiaryName("ABC Corp");
        validOrderDTO.setBeneficiaryAccount("1234567890");
        validOrderDTO.setBeneficiaryBank("XYZ Bank");
    }

    @Nested
    @DisplayName("Create Order")
    class CreateOrder {
        @Test
        void createOrder_Returns201() throws Exception {
            when(orderService.createOrder(any(PaymentOrderDTO.class))).thenReturn(validOrderDTO);

            mockMvc.perform(post("/v1/payment-orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.customerId").value("CUST001"))
                    .andExpect(jsonPath("$.data.amount").value(100.00));

            verify(orderService).createOrder(any(PaymentOrderDTO.class));
        }
    }

    @Nested
    @DisplayName("Get Order by ID")
    class GetOrderById {
        @Test
        void getOrderById_Returns200() throws Exception {
            when(orderService.getOrderById(1L)).thenReturn(validOrderDTO);

            mockMvc.perform(get("/v1/payment-orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.orderReference").value("ORD-TEST123"));

            verify(orderService).getOrderById(1L);
        }

        @Test
        void getOrderById_NotFound_Returns404() throws Exception {
            when(orderService.getOrderById(999L))
                    .thenThrow(new ResourceNotFoundException("Payment order not found with ID: 999"));

            mockMvc.perform(get("/v1/payment-orders/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Get Order by Reference")
    class GetOrderByReference {
        @Test
        void getOrderByReference_Returns200() throws Exception {
            when(orderService.getOrderByReference("ORD-TEST123")).thenReturn(validOrderDTO);

            mockMvc.perform(get("/v1/payment-orders/reference/ORD-TEST123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderReference").value("ORD-TEST123"));
        }
    }

    @Nested
    @DisplayName("Get All Orders")
    class GetAllOrders {
        @Test
        void getAllOrders_Returns200() throws Exception {
            when(orderService.getAllOrders()).thenReturn(List.of(validOrderDTO));

            mockMvc.perform(get("/v1/payment-orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("Get Orders by Customer ID")
    class GetOrdersByCustomerId {
        @Test
        void getOrdersByCustomerId_Returns200() throws Exception {
            when(orderService.getOrdersByCustomerId("CUST001")).thenReturn(List.of(validOrderDTO));

            mockMvc.perform(get("/v1/payment-orders/customer/CUST001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].customerId").value("CUST001"));
        }
    }

    @Nested
    @DisplayName("Get Orders by Status")
    class GetOrdersByStatus {
        @Test
        void getOrdersByStatus_Returns200() throws Exception {
            when(orderService.getOrdersByStatus(PaymentOrderStatus.PENDING))
                    .thenReturn(List.of(validOrderDTO));

            mockMvc.perform(get("/v1/payment-orders/status/PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("Get Orders by Date Range")
    class GetOrdersBetweenDates {
        @Test
        void getOrdersBetweenDates_Returns200() throws Exception {
            when(orderService.getOrdersBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(validOrderDTO));

            String start = "2024-01-01T00:00:00";
            String end = "2024-12-31T23:59:59";

            mockMvc.perform(get("/v1/payment-orders/date-range")
                            .param("startDate", start)
                            .param("endDate", end))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("Get Orders by Amount Range")
    class GetOrdersByAmountRange {
        @Test
        void getOrdersByAmountRange_Returns200() throws Exception {
            when(orderService.getOrdersByAmountRange(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenReturn(List.of(validOrderDTO));

            mockMvc.perform(get("/v1/payment-orders/amount-range")
                            .param("minAmount", "10")
                            .param("maxAmount", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("Update Order")
    class UpdateOrder {
        @Test
        void updateOrder_Returns200() throws Exception {
            when(orderService.updateOrder(eq(1L), any(PaymentOrderDTO.class))).thenReturn(validOrderDTO);

            mockMvc.perform(put("/v1/payment-orders/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment order updated successfully"));

            verify(orderService).updateOrder(eq(1L), any(PaymentOrderDTO.class));
        }

        @Test
        void updateOrder_InvalidOperation_Returns400() throws Exception {
            when(orderService.updateOrder(eq(1L), any(PaymentOrderDTO.class)))
                    .thenThrow(new InvalidOperationException("Cannot update order in status: COMPLETED"));

            mockMvc.perform(put("/v1/payment-orders/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validOrderDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("Update Order Status")
    class UpdateOrderStatus {
        @Test
        void updateOrderStatus_Returns200() throws Exception {
            when(orderService.updateOrderStatus(1L, PaymentOrderStatus.PROCESSING))
                    .thenReturn(validOrderDTO);

            mockMvc.perform(patch("/v1/payment-orders/1/status")
                            .param("status", "PROCESSING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Order status updated successfully"));
        }
    }

    @Nested
    @DisplayName("Cancel Order")
    class CancelOrder {
        @Test
        void cancelOrder_Returns200() throws Exception {
            doNothing().when(orderService).cancelOrder(1L);

            mockMvc.perform(patch("/v1/payment-orders/1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment order cancelled successfully"));

            verify(orderService).cancelOrder(1L);
        }
    }

    @Nested
    @DisplayName("Delete Order")
    class DeleteOrder {
        @Test
        void deleteOrder_Returns200() throws Exception {
            doNothing().when(orderService).deleteOrder(1L);

            mockMvc.perform(delete("/v1/payment-orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Payment order deleted successfully"));

            verify(orderService).deleteOrder(1L);
        }

        @Test
        void deleteOrder_NotFound_Returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Payment order not found with ID: 999"))
                    .when(orderService).deleteOrder(999L);

            mockMvc.perform(delete("/v1/payment-orders/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Customer Stats")
    class CustomerStats {
        @Test
        void getCustomerStats_Returns200() throws Exception {
            when(orderService.countOrdersByCustomerAndStatus(eq("CUST001"), any()))
                    .thenReturn(5L);
            when(orderService.getTotalAmountByCustomerAndStatus(eq("CUST001"), any()))
                    .thenReturn(new BigDecimal("2500.00"));

            mockMvc.perform(get("/v1/payment-orders/customer/CUST001/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customerId").value("CUST001"))
                    .andExpect(jsonPath("$.data.orderCount").value(5))
                    .andExpect(jsonPath("$.data.totalAmount").value(2500.00));
        }

        @Test
        void getCustomerStats_WithStatus_Returns200() throws Exception {
            when(orderService.countOrdersByCustomerAndStatus("CUST001", PaymentOrderStatus.COMPLETED))
                    .thenReturn(3L);
            when(orderService.getTotalAmountByCustomerAndStatus("CUST001", PaymentOrderStatus.COMPLETED))
                    .thenReturn(new BigDecimal("1500.00"));

            mockMvc.perform(get("/v1/payment-orders/customer/CUST001/stats")
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }
}
