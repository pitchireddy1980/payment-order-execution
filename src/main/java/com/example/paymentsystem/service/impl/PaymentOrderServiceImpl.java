package com.example.paymentsystem.service.impl;

import com.example.paymentsystem.dto.PaymentOrderDTO;
import com.example.paymentsystem.entity.PaymentOrder;
import com.example.paymentsystem.entity.PaymentOrderStatus;
import com.example.paymentsystem.exception.ResourceNotFoundException;
import com.example.paymentsystem.exception.InvalidOperationException;
import com.example.paymentsystem.repository.PaymentOrderRepository;
import com.example.paymentsystem.service.PaymentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentOrderServiceImpl implements PaymentOrderService {

    private final PaymentOrderRepository orderRepository;
    private final ModelMapper modelMapper;

    @Override
    public PaymentOrderDTO createOrder(PaymentOrderDTO orderDTO) {
        log.info("Creating new payment order for customer: {}", orderDTO.getCustomerId());

        PaymentOrder order = modelMapper.map(orderDTO, PaymentOrder.class);
        
        // Generate unique order reference
        order.setOrderReference(generateOrderReference());
        order.setStatus(PaymentOrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        PaymentOrder savedOrder = orderRepository.save(order);
        log.info("Payment order created with reference: {}", savedOrder.getOrderReference());

        return modelMapper.map(savedOrder, PaymentOrderDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderDTO getOrderById(Long id) {
        log.info("Fetching payment order with ID: {}", id);
        
        PaymentOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found with ID: " + id));
        
        return modelMapper.map(order, PaymentOrderDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOrderDTO getOrderByReference(String orderReference) {
        log.info("Fetching payment order with reference: {}", orderReference);
        
        PaymentOrder order = orderRepository.findByOrderReference(orderReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment order not found with reference: " + orderReference));
        
        return modelMapper.map(order, PaymentOrderDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderDTO> getAllOrders() {
        log.info("Fetching all payment orders");
        
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, PaymentOrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderDTO> getOrdersByCustomerId(String customerId) {
        log.info("Fetching payment orders for customer: {}", customerId);
        
        return orderRepository.findByCustomerId(customerId).stream()
                .map(order -> modelMapper.map(order, PaymentOrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderDTO> getOrdersByStatus(PaymentOrderStatus status) {
        log.info("Fetching payment orders with status: {}", status);
        
        return orderRepository.findByStatus(status).stream()
                .map(order -> modelMapper.map(order, PaymentOrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderDTO> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching orders between {} and {}", startDate, endDate);
        
        return orderRepository.findOrdersBetweenDates(startDate, endDate).stream()
                .map(order -> modelMapper.map(order, PaymentOrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentOrderDTO> getOrdersByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        log.info("Fetching orders with amount between {} and {}", minAmount, maxAmount);
        
        return orderRepository.findOrdersByAmountRange(minAmount, maxAmount).stream()
                .map(order -> modelMapper.map(order, PaymentOrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public PaymentOrderDTO updateOrderStatus(Long id, PaymentOrderStatus newStatus) {
        log.info("Updating order {} status to {}", id, newStatus);
        
        PaymentOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found with ID: " + id));

        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        
        if (newStatus == PaymentOrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }
        
        PaymentOrder updatedOrder = orderRepository.save(order);
        return modelMapper.map(updatedOrder, PaymentOrderDTO.class);
    }

    @Override
    public PaymentOrderDTO updateOrder(Long id, PaymentOrderDTO orderDTO) {
        log.info("Updating payment order with ID: {}", id);
        
        PaymentOrder existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found with ID: " + id));

        // Only allow updates for pending orders
        if (existingOrder.getStatus() != PaymentOrderStatus.PENDING) {
            throw new InvalidOperationException(
                    "Cannot update order in status: " + existingOrder.getStatus());
        }

        // Update allowed fields
        existingOrder.setCustomerName(orderDTO.getCustomerName());
        existingOrder.setCustomerEmail(orderDTO.getCustomerEmail());
        existingOrder.setDescription(orderDTO.getDescription());
        existingOrder.setUpdatedAt(LocalDateTime.now());
        
        PaymentOrder updatedOrder = orderRepository.save(existingOrder);
        return modelMapper.map(updatedOrder, PaymentOrderDTO.class);
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling payment order with ID: {}", id);
        
        PaymentOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found with ID: " + id));

        if (order.getStatus() == PaymentOrderStatus.COMPLETED || 
            order.getStatus() == PaymentOrderStatus.CANCELLED) {
            throw new InvalidOperationException(
                    "Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(PaymentOrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Deleting payment order with ID: {}", id);
        
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment order not found with ID: " + id);
        }
        
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOrdersByCustomerAndStatus(String customerId, PaymentOrderStatus status) {
        return orderRepository.countByCustomerIdAndStatus(customerId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByCustomerAndStatus(String customerId, PaymentOrderStatus status) {
        BigDecimal total = orderRepository.sumAmountByCustomerIdAndStatus(customerId, status);
        return total != null ? total : BigDecimal.ZERO;
    }

    private String generateOrderReference() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateStatusTransition(PaymentOrderStatus currentStatus, PaymentOrderStatus newStatus) {
        // Add validation logic for valid status transitions
        if (currentStatus == PaymentOrderStatus.COMPLETED && newStatus != PaymentOrderStatus.REFUNDED) {
            throw new InvalidOperationException(
                    "Completed orders can only be refunded");
        }
        
        if (currentStatus == PaymentOrderStatus.CANCELLED) {
            throw new InvalidOperationException(
                    "Cannot change status of cancelled order");
        }
    }
}
