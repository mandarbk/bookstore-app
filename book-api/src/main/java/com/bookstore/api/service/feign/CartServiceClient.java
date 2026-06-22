package com.bookstore.api.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bookstore.api.domain.LineItem;

@FeignClient(name = "cart-management-service")
public interface CartServiceClient {

    @PostMapping("/api/carts/{userId}/items")
    void addItem(@PathVariable("userId") String userId, @RequestBody LineItem item);
}
