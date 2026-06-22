package com.bookstore.gateway.config;

import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions.Builder;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayRoutes {

    private static final String BOOKS_SERVICE_CB_NAME = "books-service-cb";
    private static final String API_BOOKS = "/api/books/**";
    private static final String BOOKS_SERIVE_ROUTE = "books-service";

    @Bean
    public Function<ServerRequest, String> principalKeyResolver() {
        return request -> request.principal()
                .map(java.security.Principal::getName)
                .orElse("anonymous_user");

    }

    /**
     * The route is named books-service and matches all incoming requests starting
     * with /api/books/.
     * 
     * Authentication (Token Relay): The TokenRelayFilterFunctions.tokenRelay() is
     * applied first. This ensures that the user's OAuth2 access token, stored in
     * the server-side session, is automatically injected into the outgoing request
     * headers to the downstream service.
     * 
     * Load Balancing: The lb("books-service") filter acts as the load balancer,
     * resolving the service name to a specific instance (likely via Kubernetes
     * Discovery Or Eureka) and handling the actual request forwarding.
     * 
     * Rate Limiting: The rateLimit filter is applied last. It enforces strict
     * traffic control:
     * 
     * Capacity: Allows a maximum burst of 20 requests.
     * 
     * Refill: Replenishes 10 tokens every minute.
     * 
     * Key: Traffic is tracked per user (defined by principalKeyResolver()).
     * 
     * Penalty: Returns a 429 Too Many Requests status code if the threshold is
     * exceeded.
     * 
     * Timeout: Requests wait up to 3 seconds for a token before failing.
     * 
     * Order of Operations: By placing the TokenRelay and lb filters before the
     * rateLimit filter, you ensure that you are only counting and throttling
     * authorized requests that are successfully destined for a healthy instance of
     * your microservice.
     * 
     * Virtual Thread Compatibility: Because this uses the MVC HandlerFilterFunction
     * pattern, this entire pipeline executes synchronously on a Java Virtual
     * Thread. There is no reactive blocking; if the rateLimit filter needs to
     * pause, it simply parks the virtual thread, keeping the system responsive.
     * 
     * @return
     */
    @Bean
    public RouterFunction<ServerResponse> gatewayRouterFunctionsRateLimited() {

        HandlerFilterFunction<ServerResponse, ServerResponse> rateLimit = rateLimit(c -> {
            c.setCapacity(20).setPeriod(Duration.ofMinutes(1)).setKeyResolver(principalKeyResolver());
            c.setStatusCode(HttpStatusCode.valueOf(HttpStatus.SC_TOO_MANY_REQUESTS));
            c.setTokens(10);
            c.setTimeout(Duration.ofSeconds(3));

        });

        HandlerFilterFunction<ServerResponse, ServerResponse> circuitBreaker = CircuitBreakerFilterFunctions.circuitBreaker(BOOKS_SERVICE_CB_NAME, "forward:/fallback/service-unavailable");

        Builder route = GatewayRouterFunctions.route(BOOKS_SERIVE_ROUTE)
                .route(RequestPredicates.path(API_BOOKS), HandlerFunctions.http())
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .filter(lb("books-service"))
                .filter(circuitBreaker)
                .filter(rateLimit);

        return route.build();

    }

    @Bean
    public RouterFunction<ServerResponse> userFallbackRoute() {

        HandlerFunction<ServerResponse> fallbackHandlerFunction = request -> ServerResponse.status(HttpStatus.SC_SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Service currently unavailable, please try again later."));

        Builder route = GatewayRouterFunctions.route("fallbackRoute")
                .route(RequestPredicates.path("/fallback/service-unavailable"), fallbackHandlerFunction);

        return route.build();

    }

}
