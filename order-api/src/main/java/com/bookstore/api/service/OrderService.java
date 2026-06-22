package com.bookstore.api.service;

import com.bookstore.api.dto.OrderDto;
import com.bookstore.api.entity.Order;
import com.bookstore.api.repository.jdbc.OrderJdbcRepository;
import com.bookstore.api.repository.jpa.OrderJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {
    private final OrderJdbcRepository orderJdbcRepository;
    private final OrderJpaRepository orderJpaRepository;

    public OrderService(OrderJdbcRepository orderJdbcRepository, OrderJpaRepository orderJpaRepository) {
        this.orderJdbcRepository = orderJdbcRepository;
        this.orderJpaRepository = orderJpaRepository;
    }

    // READ operations - Using JDBC with fallback
    @Transactional(readOnly = true)
    public Optional<OrderDto> getOrderById(Long id) {
        log.debug("Fetching order by id: {}", id);
        return orderJdbcRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        log.debug("Fetching all orders");
        return orderJdbcRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        log.debug("Fetching orders for customer id: {}", customerId);
        return orderJdbcRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(String status) {
        log.debug("Fetching orders with status: {}", status);
        return orderJdbcRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Long getTotalOrdersCount() {
        return orderJdbcRepository.countAll();
    }

    public Long getOrderCountByCustomerId(Long customerId) {
        return orderJdbcRepository.countByCustomerId(customerId);
    }

    // WRITE operations - Using JPA
    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating new order for customer id: {}", order.getCustomerId());
        return orderJpaRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Long id, Order orderUpdates) {
        log.info("Updating order with id: {}", id);
        Order order = orderJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        if (orderUpdates.getStatus() != null) order.setStatus(orderUpdates.getStatus());
        if (orderUpdates.getTotalPrice() != null) order.setTotalPrice(orderUpdates.getTotalPrice());

        return orderJpaRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);
        orderJpaRepository.deleteById(id);
    }

    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        log.info("Updating order status for id: {}, status: {}", id, status);
        Order order = orderJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        order.setStatus(status);
        return orderJpaRepository.save(order);
    }
}
