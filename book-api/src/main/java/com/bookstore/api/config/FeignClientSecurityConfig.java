package com.bookstore.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignClientSecurityConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        // FULL logs the request/response headers, body data, and metadata
        return Logger.Level.FULL; 
    }

    @Bean
    public RequestInterceptor securityRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. Safely extract the current inbound HTTP request context
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // 2. Propagate JWT Token (Authorization Header)
                    String authorizationHeader = request.getHeader("Authorization");
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        template.header("Authorization", authorizationHeader);
                    }
                }
            }
        };
    }
}

