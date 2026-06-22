package com.bookstore.api.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.bookstore.api.config.FeignClientSecurityConfig;

@FeignClient(name = "order-service", 
        configuration = { FeignClientSecurityConfig.class,
                                FeignSessionInterceptorConfig.class })
public interface OrderServiceClient {

    @PostMapping("/api/orders/{userId}")
    void placeOrder(@PathVariable("userId") String userId);
}
