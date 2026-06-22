package com.bookstore.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@RestController
@RequestMapping("/debug")
public class DebugClientController {

    private final RegisteredClientRepository clientRepository;

    // Inject the exact bean handling your client storage
    public DebugClientController(RegisteredClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public RegisteredClientDetails getClientDetails(@RequestParam String clientId) {
        RegisteredClient client = this.clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new IllegalArgumentException("No client found matching ID: " + clientId);
        }
        
        // Map to a clean presentation DTO (Avoid returning RegisteredClient directly due to circular references)
        return new RegisteredClientDetails(
            client.getClientId(),
            client.getRedirectUris(),
            client.getScopes(),
            client.getAuthorizationGrantTypes()
        );
    }
}

// Simple record DTO to avoid serialization failures
record RegisteredClientDetails(
    String clientId, 
    java.util.Set<String> redirectUris, 
    java.util.Set<String> scopes, 
    java.util.Set<org.springframework.security.oauth2.core.AuthorizationGrantType> grantTypes
) {}

