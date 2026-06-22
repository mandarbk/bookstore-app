package com.bookstore.api.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookstore.api.domain.Cart;

@FeignClient(name = "cart-management-service")
public interface CartServiceClient {

    @GetMapping("/api/carts/{userId}")
    Cart getCart(@PathVariable("userId") String userId);
}
