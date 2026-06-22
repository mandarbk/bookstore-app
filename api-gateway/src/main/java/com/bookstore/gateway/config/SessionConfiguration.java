package com.bookstore.gateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
public class SessionConfiguration {

    // @Bean
    // public JsonMapper jsonMapper() {
    // ClassLoader loader = getClass().getClassLoader();

    // PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
    // .allowIfBaseType("org.springframework.security.")
    // .allowIfBaseType("java.util.")
    // .allowIfSubType("java.net.URL")
    // .allowIfSubType("java.net.URI")
    // .allowIfSubType("java.lang.String")
    // .allowIfSubType("java.time.")
    // .build();

    // return JsonMapper.builder()
    // .polymorphicTypeValidator(ptv)
    // .addModules(SecurityJacksonModules.getModules(loader))
    // .build();
    // }

    // TODO : remember to switch back to Jackson3 when the issues are resolved.

    // @Bean
    // public RedisSerializer<Object> springSessionDefaultRedisSerializer(JsonMapper
    // jsonMapper) {
    // // Automatically serializes your session data to human-readable JSON strings
    // // inside Redis
    // ObjectMapper objectMapper = jsonMapper;

    // return new GenericJackson2JsonRedisSerializer(jsonMapper);
    // }

    /**
     * Workaround for
     * https://github.com/spring-projects/spring-security/issues/19077
     * Using Jackson 2 serializer to avoid ClassCastException/SerializationException
     * until upstream fixes polymorphic type support for Jackson 3.
     */
    @SuppressWarnings("removal")
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        // 1. Create the Validator
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("org.springframework.security.")
                .allowIfBaseType("java.util.")
                .allowIfSubType("java.net.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.lang.")
                .build();

        // 2. Initialize ObjectMapper manually (avoiding builder() issues)
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPolymorphicTypeValidator(ptv);

        List<Module> modules = SecurityJackson2Modules.getModules(getClass().getClassLoader());
        modules.forEach(module -> mapper.registerModule(module));

        // 4. Return the serializer
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

}
