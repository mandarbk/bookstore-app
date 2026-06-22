package com.bookstore.gateway.config;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.web.servlet.function.ServerRequest;

import io.github.bucket4j.distributed.proxy.AsyncProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Function<ServerRequest, String> ipKeyResolver() {
        return request -> request.remoteAddress()
                .map(address -> address.getAddress().getHostAddress())
                .orElse("anonymous_ip");
    }

    /**
     * This bean extracts the native Lettuce RedisClient from the 
     * connection factory managed by Spring Boot.
     */
    @Bean
    public RedisClient redisClient(LettuceConnectionFactory lettuceConnectionFactory) {
        // The factory holds the reference to the native client resources
        return (RedisClient) lettuceConnectionFactory.getNativeClient();
    }

    @Bean
    public ProxyManager<String> lettuceBasedProxyManager(RedisClient redisClient) {
        // Reuse the same RedisClient that your session management uses
        StatefulRedisConnection<String, byte[]> connection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(connection)
                .build();
    }

    // 2. Add the AsyncProxyManager bean that the Gateway MVC filter actually looks for
    @Bean
    public AsyncProxyManager<String> asyncProxyManager(ProxyManager<String> proxyManager) {
        return proxyManager.asAsync();
    }

    

    // @Bean
    // public Function<String, Bucket> bucketResolver(ProxyManager<String>
    // proxyManager) {
    // return key -> proxyManager.builder().build(key, () -> {

    // // Capacity: 50 (Maximum burst)
    // // Refill: 10 tokens every 1 minute (Sustained rate)
    // // This allows a burst of 50 requests instantly,
    // // but averages 10 requests per minute thereafter.

    // Bandwidth limit =
    // BandwidthBuilder.builder().capacity(20).refillIntervally(10,
    // Duration.ofMinutes(1))
    // .build();

    // return BucketConfiguration.builder()
    // .addLimit(limit)
    // .build();
    // });
    // }
}
