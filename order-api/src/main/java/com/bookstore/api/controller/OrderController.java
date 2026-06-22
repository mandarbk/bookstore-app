package com.bookstore.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.api.domain.Cart;
import com.bookstore.api.dto.OrderDto;
import com.bookstore.api.entity.Order;
import com.bookstore.api.service.OrderService;
import com.bookstore.api.service.feign.CartServiceClient;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    private final CartServiceClient cartService;

    public OrderController(OrderService orderService, CartServiceClient cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SCOPE_read:orders')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.info("GET /api/orders - Fetching all orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:orders')")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        log.info("GET /api/orders/{} - Fetching order by id", id);
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:orders')")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(@PathVariable Long customerId) {
        log.info("GET /api/orders/customer/{} - Fetching orders for customer", customerId);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:orders')")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable String status) {
        log.info("GET /api/orders/status/{} - Fetching orders with status", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_write:orders')")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderUpdates) {
        log.info("PUT /api/orders/{} - Updating order status", orderUpdates.getStatus());
        try {
            return ResponseEntity.ok(orderService.updateOrder(id, orderUpdates));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('SCOPE_write:orders')")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("PUT /api/orders/{}/status - Updating order status", id);
        try {
            return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCOPE_write:orders')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.info("DELETE /api/orders/{} - Deleting order", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('SCOPE_read:orders')")
    public ResponseEntity<Long> countAllOrders() {
        log.info("GET /api/orders/count - Counting all orders");
        return ResponseEntity.ok(orderService.getTotalOrdersCount());
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> placeOrder(@PathVariable("userId") String userId) {

        log.info("Post /api/orders - Placing orders for {}", userId);

        Cart cart = cartService.getCart(userId);
        
        log.info("Creating orders from cart {} ", cart);
        return ResponseEntity.ok().build();
    }
    
}
