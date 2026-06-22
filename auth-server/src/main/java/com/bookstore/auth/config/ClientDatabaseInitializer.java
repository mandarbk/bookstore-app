package com.bookstore.auth.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@DependsOnDatabaseInitialization
public class ClientDatabaseInitializer implements CommandLineRunner {

    private final RegisteredClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private JdbcUserDetailsManager userDetailsManager;

    public ClientDatabaseInitializer(RegisteredClientRepository clientRepository, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsManager = userDetailsService instanceof JdbcUserDetailsManager ? (JdbcUserDetailsManager) userDetailsService : null;
    }

    @Override
    public void run(String... args) throws Exception {
        // If our Gateway client isn't in the DB yet, insert it programmatically
        if (clientRepository.findByClientId("api-gateway-client") == null) {

            RegisteredClient gatewayClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("api-gateway-client")
                    .clientSecret("123456")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://localhost:8080/login/oauth2/code/api-gateway-client")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope("offline_access")
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(false)
                            .requireProofKey(false)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofHours(1))
                            .build())
                    .build();

            clientRepository.save(gatewayClient);
        }

        addUsersToDatabase();
    }

    private void addUsersToDatabase() {
        if (!userDetailsManager.userExists("admin")) {
            UserDetails admin = User.withUsername("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .roles("ADMIN")
                    .build();
            userDetailsManager.createUser(admin);
        }

        if (!userDetailsManager.userExists("user")) {
            UserDetails admin = User.withUsername("user")
                    .password(passwordEncoder.encode("user123"))
                    .roles("USER")
                    .build();
            userDetailsManager.createUser(admin);
        }
    }
}
