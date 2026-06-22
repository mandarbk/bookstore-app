package com.bookstore.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import jakarta.annotation.Resource;

@RestController
public class DebugRoutesController {

    @Resource(name="gatewayRouterFunctionsRateLimited")
    private RouterFunction<ServerResponse> gatewayRouterFunctions;

    @GetMapping("/actuator/custom-gateway/routes")
    public String getGatewayRoutes() {
        // Returns the string structure of the compiled functional routing blocks
        return this.gatewayRouterFunctions.toString();
    }
}

