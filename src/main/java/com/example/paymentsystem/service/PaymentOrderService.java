package com.example.paymentsystem.service;

import com.example.paymentsystem.dto.PaymentOrderDTO;
import com.example.paymentsystem.entity.PaymentOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentOrderService {

    PaymentOrderDTO createOrder(PaymentOrderDTO orderDTO);

    PaymentOrderDTO getOrderById(Long id);

    PaymentOrderDTO getOrderByReference(String orderReference);

    List<PaymentOrderDTO> getAllOrders();

    List<PaymentOrderDTO> getOrdersByCustomerId(String customerId);

    List<PaymentOrderDTO> getOrdersByStatus(PaymentOrderStatus status);

    List<PaymentOrderDTO> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    List<PaymentOrderDTO> getOrdersByAmountRange(BigDecimal minAmount, BigDecimal maxAmount);

    PaymentOrderDTO updateOrderStatus(Long id, PaymentOrderStatus newStatus);

    PaymentOrderDTO updateOrder(Long id, PaymentOrderDTO orderDTO);

    void cancelOrder(Long id);

    void deleteOrder(Long id);

    Long countOrdersByCustomerAndStatus(String customerId, PaymentOrderStatus status);

    BigDecimal getTotalAmountByCustomerAndStatus(String customerId, PaymentOrderStatus status);
}
