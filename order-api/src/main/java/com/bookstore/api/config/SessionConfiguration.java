package com.bookstore.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import tools.jackson.databind.ObjectMapper;

@Configuration
public class SessionConfiguration {

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(ObjectMapper objectMapper) {
        // Automatically serializes your session data to human-readable JSON strings
        // inside Redis
        return new GenericJacksonJsonRedisSerializer(objectMapper);
    }

}


