package com.example.paymentsystem.service;

import com.example.paymentsystem.dto.PaymentExecutionDTO;
import com.example.paymentsystem.entity.*;
import com.example.paymentsystem.exception.InvalidOperationException;
import com.example.paymentsystem.exception.ResourceNotFoundException;
import com.example.paymentsystem.repository.PaymentExecutionRepository;
import com.example.paymentsystem.repository.PaymentOrderRepository;
import com.example.paymentsystem.service.impl.PaymentExecutionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentExecutionServiceTest {

    @Mock
    private PaymentExecutionRepository executionRepository;

    @Mock
    private PaymentOrderRepository orderRepository;

    @Mock
    private PaymentGatewayService gatewayService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentExecutionServiceImpl executionService;

    private PaymentOrder testOrder;
    private PaymentExecution testExecution;
    private PaymentExecutionDTO testExecutionDTO;

    @BeforeEach
    void setUp() {
        testOrder = new PaymentOrder();
        testOrder.setId(1L);
        testOrder.setOrderReference("ORD-TEST123");
        testOrder.setCustomerId("CUST001");
        testOrder.setAmount(new BigDecimal("100.00"));
        testOrder.setCurrency("USD");
        testOrder.setStatus(PaymentOrderStatus.PENDING);

        testExecution = new PaymentExecution();
        testExecution.setId(1L);
        testExecution.setExecutionReference("EXE-TEST123");
        testExecution.setPaymentOrder(testOrder);
        testExecution.setAmount(new BigDecimal("100.00"));
        testExecution.setCurrency("USD");
        testExecution.setStatus(ExecutionStatus.SUCCESS);
        testExecution.setRetryAttempt(0);

        testExecutionDTO = new PaymentExecutionDTO();
        testExecutionDTO.setId(1L);
        testExecutionDTO.setExecutionReference("EXE-TEST123");
        testExecutionDTO.setPaymentOrderId(1L);
        testExecutionDTO.setOrderReference("ORD-TEST123");
        testExecutionDTO.setStatus(ExecutionStatus.SUCCESS);
        testExecutionDTO.setAmount(new BigDecimal("100.00"));
        testExecutionDTO.setCurrency("USD");
    }

    @Nested
    @DisplayName("Execute Payment")
    class ExecutePayment {
        @Test
        void executePayment_Success() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
            when(executionRepository.save(any(PaymentExecution.class))).thenAnswer(inv -> {
                PaymentExecution e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });
            when(gatewayService.processPayment(any(PaymentExecution.class), any(PaymentOrder.class)))
                    .thenReturn(true);
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            PaymentExecutionDTO result = executionService.executePayment(1L);

            assertNotNull(result);
            verify(orderRepository).findById(1L);
            verify(executionRepository, atLeast(1)).save(any(PaymentExecution.class));
            verify(gatewayService).processPayment(any(PaymentExecution.class), eq(testOrder));
        }

        @Test
        void executePayment_OrderNotFound_ThrowsResourceNotFoundException() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> executionService.executePayment(999L));
            verify(executionRepository, never()).save(any());
        }

        @Test
        void executePayment_OrderAlreadyCompleted_ThrowsInvalidOperation() {
            testOrder.setStatus(PaymentOrderStatus.COMPLETED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(InvalidOperationException.class, () -> executionService.executePayment(1L));
            verify(executionRepository, never()).save(any());
        }

        @Test
        void executePayment_OrderCancelled_ThrowsInvalidOperation() {
            testOrder.setStatus(PaymentOrderStatus.CANCELLED);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

            assertThrows(InvalidOperationException.class, () -> executionService.executePayment(1L));
        }
    }

    @Nested
    @DisplayName("Get Execution by ID")
    class GetExecutionById {
        @Test
        void getExecutionById_Success() {
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            PaymentExecutionDTO result = executionService.getExecutionById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(executionRepository).findById(1L);
        }

        @Test
        void getExecutionById_NotFound() {
            when(executionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> executionService.getExecutionById(999L));
        }
    }

    @Nested
    @DisplayName("Get Execution by Reference")
    class GetExecutionByReference {
        @Test
        void getExecutionByReference_Success() {
            when(executionRepository.findByExecutionReference("EXE-TEST123"))
                    .thenReturn(Optional.of(testExecution));
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            PaymentExecutionDTO result = executionService.getExecutionByReference("EXE-TEST123");

            assertNotNull(result);
            assertEquals("EXE-TEST123", result.getExecutionReference());
        }

        @Test
        void getExecutionByReference_NotFound() {
            when(executionRepository.findByExecutionReference("EXE-NOTFOUND"))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> executionService.getExecutionByReference("EXE-NOTFOUND"));
        }
    }

    @Nested
    @DisplayName("Get Executions by Order ID")
    class GetExecutionsByOrderId {
        @Test
        void getExecutionsByOrderId_Success() {
            when(executionRepository.findExecutionsByOrderIdOrderByCreatedAtDesc(1L))
                    .thenReturn(Arrays.asList(testExecution));
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            List<PaymentExecutionDTO> result = executionService.getExecutionsByOrderId(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Get Executions by Status")
    class GetExecutionsByStatus {
        @Test
        void getExecutionsByStatus_Success() {
            when(executionRepository.findByStatus(ExecutionStatus.SUCCESS))
                    .thenReturn(Arrays.asList(testExecution));
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            List<PaymentExecutionDTO> result = executionService.getExecutionsByStatus(ExecutionStatus.SUCCESS);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(ExecutionStatus.SUCCESS, result.get(0).getStatus());
        }
    }

    @Nested
    @DisplayName("Update Execution Status")
    class UpdateExecutionStatus {
        @Test
        void updateExecutionStatus_ToSuccess_UpdatesOrder() {
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
            when(executionRepository.save(any(PaymentExecution.class))).thenReturn(testExecution);
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            PaymentExecutionDTO result = executionService.updateExecutionStatus(1L, ExecutionStatus.SUCCESS);

            assertNotNull(result);
            verify(executionRepository).save(any(PaymentExecution.class));
            verify(orderRepository).save(any(PaymentOrder.class));
        }

        @Test
        void updateExecutionStatus_NotFound() {
            when(executionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> executionService.updateExecutionStatus(999L, ExecutionStatus.SUCCESS));
        }
    }

    @Nested
    @DisplayName("Retry Execution")
    class RetryExecution {
        @Test
        void retryExecution_Success() {
            testExecution.setStatus(ExecutionStatus.FAILED);
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
            when(executionRepository.save(any(PaymentExecution.class))).thenAnswer(inv -> {
                PaymentExecution e = inv.getArgument(0);
                e.setId(2L);
                return e;
            });
            when(gatewayService.processPayment(any(PaymentExecution.class), any(PaymentOrder.class)))
                    .thenReturn(true);
            when(modelMapper.map(any(PaymentExecution.class), eq(PaymentExecutionDTO.class)))
                    .thenReturn(testExecutionDTO);

            PaymentExecutionDTO result = executionService.retryExecution(1L);

            assertNotNull(result);
            verify(executionRepository, atLeast(1)).save(any(PaymentExecution.class));
        }

        @Test
        void retryExecution_NotFailed_ThrowsInvalidOperation() {
            testExecution.setStatus(ExecutionStatus.SUCCESS);
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

            assertThrows(InvalidOperationException.class, () -> executionService.retryExecution(1L));
            verify(executionRepository, never()).save(any(PaymentExecution.class));
        }

        @Test
        void retryExecution_NotFound() {
            when(executionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> executionService.retryExecution(999L));
        }
    }

    @Nested
    @DisplayName("Process Settlement")
    class ProcessSettlement {
        @Test
        void processSettlement_Success() {
            testExecution.setStatus(ExecutionStatus.SUCCESS);
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
            when(executionRepository.save(any(PaymentExecution.class))).thenReturn(testExecution);

            assertDoesNotThrow(() -> executionService.processSettlement(1L));

            ArgumentCaptor<PaymentExecution> captor = ArgumentCaptor.forClass(PaymentExecution.class);
            verify(executionRepository).save(captor.capture());
            assertEquals(ExecutionStatus.SETTLED, captor.getValue().getStatus());
        }

        @Test
        void processSettlement_NotSuccess_ThrowsInvalidOperation() {
            testExecution.setStatus(ExecutionStatus.FAILED);
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

            assertThrows(InvalidOperationException.class, () -> executionService.processSettlement(1L));
            verify(executionRepository, never()).save(any(PaymentExecution.class));
        }

        @Test
        void processSettlement_NotFound() {
            when(executionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> executionService.processSettlement(999L));
        }
    }

    @Nested
    @DisplayName("Reverse Execution")
    class ReverseExecution {
        @Test
        void reverseExecution_Success() {
            testExecution.setStatus(ExecutionStatus.SUCCESS);
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
            when(executionRepository.save(any(PaymentExecution.class))).thenReturn(testExecution);
            when(orderRepository.save(any(PaymentOrder.class))).thenReturn(testOrder);

            assertDoesNotThrow(() -> executionService.reverseExecution(1L));

            verify(executionRepository).save(any(PaymentExecution.class));
            verify(orderRepository).save(any(PaymentOrder.class));
        }

        @Test
        void reverseExecution_NotSuccessOrSettled_ThrowsInvalidOperation() {
            testExecution.setStatus(ExecutionStatus.FAILED);
            when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

            assertThrows(InvalidOperationException.class, () -> executionService.reverseExecution(1L));
        }

        @Test
        void reverseExecution_NotFound() {
            when(executionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> executionService.reverseExecution(999L));
        }
    }
}
