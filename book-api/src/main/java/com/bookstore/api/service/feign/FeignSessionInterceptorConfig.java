package com.bookstore.api.service.feign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignSessionInterceptorConfig {

    @Bean
    public RequestInterceptor sessionCookieForwardingInterceptor() {
        return requestTemplate -> {
            // 1. Grab the inbound HTTP request attributes from the current executing thread
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest inboundRequest = attributes.getRequest();
                
                // 2. Extract the "Cookie" header (contains JSESSIONID / SESSION cookie)
                String cookieHeader = inboundRequest.getHeader(HttpHeaders.COOKIE);
                
                if (cookieHeader != null && !cookieHeader.isBlank()) {
                    // 3. Forward the exact cookie string down to the target microservice
                    requestTemplate.header(HttpHeaders.COOKIE, cookieHeader);
                }
            }
        };
    }
}
