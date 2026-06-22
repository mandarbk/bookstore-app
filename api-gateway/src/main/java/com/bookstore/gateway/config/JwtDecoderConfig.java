package com.bookstore.gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JwtDecoderConfig {

    @Value("${jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public JwtDecoder jwtDecoder(@Lazy LoadBalancerInterceptor loadBalancerInterceptor) {
        // 1. Create a standard RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // 2. Explicitly inject the Eureka LoadBalancer interceptor into it
        restTemplate.setInterceptors(List.of(loadBalancerInterceptor));

        // 3. Construct the decoder using our load-balanced RestTemplate
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .restOperations(restTemplate)
                .build();
    }

}

