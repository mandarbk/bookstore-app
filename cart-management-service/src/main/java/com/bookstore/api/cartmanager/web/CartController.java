package com.bookstore.api.cartmanager.web;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookstore.api.cartmanager.repository.CartRepository;
import com.bookstore.api.domain.Cart;
import com.bookstore.api.domain.LineItem;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/carts")
@Slf4j
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // Get current user's cart
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        return cartRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add item to cart
    @PostMapping("/{userId}/items") 
    public ResponseEntity<Cart> addItem(@PathVariable String userId, @RequestBody LineItem item) {
        Cart cart = cartRepository.findById(userId).orElse(new Cart(userId, new ArrayList<>()));
        cart.getItems().add(item);
        cartRepository.save(cart);
        return ResponseEntity.ok(cartRepository.findById(userId).get());
    }

    // Clear cart after checkout
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartRepository.deleteById(userId);
        return ResponseEntity.noContent().build();
    }
}
