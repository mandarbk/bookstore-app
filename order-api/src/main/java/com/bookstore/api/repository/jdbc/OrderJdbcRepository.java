package com.bookstore.api.repository.jdbc;

import com.bookstore.api.dto.OrderDto;
import com.bookstore.api.util.ReadFallbackTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class OrderJdbcRepository {
    private final ReadFallbackTemplate fallbackTemplate;

    public OrderJdbcRepository(ReadFallbackTemplate fallbackTemplate) {
        this.fallbackTemplate = fallbackTemplate;
    }

    public Optional<OrderDto> findById(Long id) {
        String sql = "SELECT id, customer_id, order_date, total_price, status, created_at, updated_at FROM orders WHERE id = ?";
        try {
            OrderDto order = fallbackTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(OrderDto.class), id);
            return Optional.ofNullable(order);
        } catch (Exception e) {
            log.error("Error finding order by id: {}", id, e);
            return Optional.empty();
        }
    }

    public List<OrderDto> findAll() {
        String sql = "SELECT id, customer_id, order_date, total_price, status, created_at, updated_at FROM orders ORDER BY order_date DESC";
        return fallbackTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDto.class));
    }

    public List<OrderDto> findByCustomerId(Long customerId) {
        String sql = "SELECT id, customer_id, order_date, total_price, status, created_at, updated_at FROM orders WHERE customer_id = ? ORDER BY order_date DESC";
        return fallbackTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDto.class), customerId);
    }

    public List<OrderDto> findByStatus(String status) {
        String sql = "SELECT id, customer_id, order_date, total_price, status, created_at, updated_at FROM orders WHERE status = ? ORDER BY order_date DESC";
        return fallbackTemplate.query(sql, new BeanPropertyRowMapper<>(OrderDto.class), status);
    }

    public Long countAll() {
        String sql = "SELECT COUNT(*) FROM orders";
        Long count = fallbackTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    public Long countByCustomerId(Long customerId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE customer_id = ?";
        Long count = fallbackTemplate.queryForObject(sql, Long.class, customerId);
        return count != null ? count : 0L;
    }
}
