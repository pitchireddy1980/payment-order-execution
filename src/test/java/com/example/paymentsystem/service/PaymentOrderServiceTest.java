package com.example.paymentsystem.service;

import com.example.paymentsystem.dto.PaymentOrderDTO;
import com.example.paymentsystem.entity.PaymentMethod;
import com.example.paymentsystem.entity.PaymentOrder;
import com.example.paymentsystem.entity.PaymentOrderStatus;
import com.example.paymentsystem.exception.InvalidOperationException;
import com.example.paymentsystem.exception.ResourceNotFoundException;
import com.example.paymentsystem.repository.PaymentOrderRepository;
import com.example.paymentsystem.service.impl.PaymentOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentOrderServiceTest {

    @Mock
    private PaymentOrderRepository orderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentOrderServiceImpl orderService;

    private PaymentOrder testOrder;
    private PaymentOrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        // Setup test order
        testOrder = new PaymentOrder();
        testOrder.setId(1L);
        testOrder.setOrderReference("ORD-TEST123");
        testOrder.setCustomerId("CUST001");
        testOrder.setCustomerName("John Doe");
        testOrder.setCustomerEmail("john@example.com");
        testOrder.setAmount(new BigDecimal("1000.00"));
        testOrder.setCurrency("USD");
        testOrder.setStatus(PaymentOrderStatus.PENDING);
        testOrder.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        testOrder.setBeneficiaryName("ABC Corp");
        testOrder.setBeneficiaryAccount("1234567890");
        testOrder.setBeneficiaryBank("XYZ Bank");

        // Setup test DTO
        testOrderDTO = new PaymentOrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setOrderReference("ORD-TEST123");
        testOrderDTO.setCustomerId("CUST001");
        testOrderDTO.setCustomerName("John Doe");
        testOrderDTO.setCustomerEmail("john@example.com");
        testOrderDTO.setAmount(new BigDecimal("1000.00"));
        testOrderDTO.setCurrency("USD");
        testOrderDTO.setStatus(PaymentOrderStatus.PENDING);
        testOrderDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        testOrderDTO.setBeneficiaryName("ABC Corp");
        testOrderDTO.setBeneficiaryAccount("1234567890");
        testOrderDTO.setBeneficiaryBank("XYZ Bank");
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        when(modelMapper.map(testOrderDTO, PaymentOrder.class)).thenReturn(testOrder);
        when(orderRepository.save(any(PaymentOrder.class))).thenReturn(testOrder);
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);

        // Act
        PaymentOrderDTO result = orderService.createOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO.getCustomerId(), result.getCustomerId());
        assertEquals(testOrderDTO.getAmount(), result.getAmount());
        verify(orderRepository, times(1)).save(any(PaymentOrder.class));
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);

        // Act
        PaymentOrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });
    }

    @Test
    void testGetOrderByReference_Success() {
        // Arrange
        when(orderRepository.findByOrderReference("ORD-TEST123"))
                .thenReturn(Optional.of(testOrder));
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);

        // Act
        PaymentOrderDTO result = orderService.getOrderByReference("ORD-TEST123");

        // Assert
        assertNotNull(result);
        assertEquals("ORD-TEST123", result.getOrderReference());
    }

    @Test
    void testGetOrdersByCustomerId_Success() {
        // Arrange
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        when(orderRepository.findByCustomerId("CUST001")).thenReturn(orders);
        when(modelMapper.map(any(PaymentOrder.class), eq(PaymentOrderDTO.class)))
                .thenReturn(testOrderDTO);

        // Act
        List<PaymentOrderDTO> result = orderService.getOrdersByCustomerId("CUST001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CUST001", result.get(0).getCustomerId());
    }

    @Test
    void testGetOrdersByStatus_Success() {
        // Arrange
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(PaymentOrderStatus.PENDING)).thenReturn(orders);
        when(modelMapper.map(any(PaymentOrder.class), eq(PaymentOrderDTO.class)))
                .thenReturn(testOrderDTO);

        // Act
        List<PaymentOrderDTO> result = orderService.getOrdersByStatus(PaymentOrderStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(PaymentOrderStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void testUpdateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(PaymentOrder.class))).thenReturn(testOrder);
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);

        // Act
        PaymentOrderDTO result = orderService.updateOrderStatus(1L, PaymentOrderStatus.PROCESSING);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(PaymentOrder.class));
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(PaymentOrder.class))).thenReturn(testOrder);

        // Act
        assertDoesNotThrow(() -> orderService.cancelOrder(1L));

        // Assert
        verify(orderRepository, times(1)).save(any(PaymentOrder.class));
    }

    @Test
    void testDeleteOrder_Success() {
        // Arrange
        when(orderRepository.existsById(1L)).thenReturn(true);
        doNothing().when(orderRepository).deleteById(1L);

        // Act
        assertDoesNotThrow(() -> orderService.deleteOrder(1L));

        // Assert
        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCountOrdersByCustomerAndStatus() {
        // Arrange
        when(orderRepository.countByCustomerIdAndStatus("CUST001", PaymentOrderStatus.PENDING))
                .thenReturn(5L);

        // Act
        Long count = orderService.countOrdersByCustomerAndStatus("CUST001", PaymentOrderStatus.PENDING);

        // Assert
        assertEquals(5L, count);
    }

    @Test
    void testGetTotalAmountByCustomerAndStatus() {
        // Arrange
        BigDecimal expectedTotal = new BigDecimal("5000.00");
        when(orderRepository.sumAmountByCustomerIdAndStatus("CUST001", PaymentOrderStatus.COMPLETED))
                .thenReturn(expectedTotal);

        // Act
        BigDecimal total = orderService.getTotalAmountByCustomerAndStatus(
                "CUST001", PaymentOrderStatus.COMPLETED);

        // Assert
        assertEquals(expectedTotal, total);
    }

    @Test
    void testGetOrderByReference_NotFound() {
        when(orderRepository.findByOrderReference("ORD-NOTFOUND")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderByReference("ORD-NOTFOUND");
        });
    }

    @Test
    void testGetAllOrders_Success() {
        List<PaymentOrder> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);
        when(modelMapper.map(any(PaymentOrder.class), eq(PaymentOrderDTO.class))).thenReturn(testOrderDTO);

        List<PaymentOrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllOrders_Empty() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<PaymentOrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOrdersBetweenDates_Success() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        when(orderRepository.findOrdersBetweenDates(start, end)).thenReturn(Arrays.asList(testOrder));
        when(modelMapper.map(any(PaymentOrder.class), eq(PaymentOrderDTO.class))).thenReturn(testOrderDTO);

        List<PaymentOrderDTO> result = orderService.getOrdersBetweenDates(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetOrdersByAmountRange_Success() {
        BigDecimal min = new BigDecimal("50");
        BigDecimal max = new BigDecimal("500");
        when(orderRepository.findOrdersByAmountRange(min, max)).thenReturn(Arrays.asList(testOrder));
        when(modelMapper.map(any(PaymentOrder.class), eq(PaymentOrderDTO.class))).thenReturn(testOrderDTO);

        List<PaymentOrderDTO> result = orderService.getOrdersByAmountRange(min, max);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(PaymentOrder.class))).thenReturn(testOrder);
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);

        PaymentOrderDTO result = orderService.updateOrder(1L, testOrderDTO);

        assertNotNull(result);
        verify(orderRepository).save(any(PaymentOrder.class));
    }

    @Test
    void testUpdateOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrder(999L, testOrderDTO);
        });
    }

    @Test
    void testUpdateOrder_NonPending_ThrowsInvalidOperation() {
        testOrder.setStatus(PaymentOrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidOperationException.class, () -> {
            orderService.updateOrder(1L, testOrderDTO);
        });
        verify(orderRepository, never()).save(any(PaymentOrder.class));
    }

    @Test
    void testUpdateOrderStatus_CompletedToRefunded_Success() {
        testOrder.setStatus(PaymentOrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(PaymentOrder.class))).thenReturn(testOrder);
        when(modelMapper.map(testOrder, PaymentOrderDTO.class)).thenReturn(testOrderDTO);

        PaymentOrderDTO result = orderService.updateOrderStatus(1L, PaymentOrderStatus.REFUNDED);

        assertNotNull(result);
        verify(orderRepository).save(any(PaymentOrder.class));
    }

    @Test
    void testUpdateOrderStatus_CompletedToOther_ThrowsInvalidOperation() {
        testOrder.setStatus(PaymentOrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidOperationException.class, () -> {
            orderService.updateOrderStatus(1L, PaymentOrderStatus.PROCESSING);
        });
    }

    @Test
    void testUpdateOrderStatus_Cancelled_ThrowsInvalidOperation() {
        testOrder.setStatus(PaymentOrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidOperationException.class, () -> {
            orderService.updateOrderStatus(1L, PaymentOrderStatus.PROCESSING);
        });
    }

    @Test
    void testCancelOrder_WhenCompleted_ThrowsInvalidOperation() {
        testOrder.setStatus(PaymentOrderStatus.COMPLETED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidOperationException.class, () -> orderService.cancelOrder(1L));
        verify(orderRepository, never()).save(any(PaymentOrder.class));
    }

    @Test
    void testCancelOrder_WhenAlreadyCancelled_ThrowsInvalidOperation() {
        testOrder.setStatus(PaymentOrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(InvalidOperationException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(999L));
        verify(orderRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetTotalAmountByCustomerAndStatus_NullReturnsZero() {
        when(orderRepository.sumAmountByCustomerIdAndStatus("CUST001", PaymentOrderStatus.PENDING))
                .thenReturn(null);

        BigDecimal total = orderService.getTotalAmountByCustomerAndStatus(
                "CUST001", PaymentOrderStatus.PENDING);

        assertEquals(BigDecimal.ZERO, total);
    }
}
