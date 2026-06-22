package com.bookstore.auth.config;

import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import com.bookstore.ds.config.DataSourceConfig;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@AutoConfigureAfter(DataSourceConfig.class)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        // Explicitly forces Spring to inject the lazy routing proxy wrapper bean
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/debug",
                "/debug/**",
                "/health",
                "/actuator/**");
    }

    // THIS IS NOW THE ONLY FILTER CHAIN BEAN IN THE CLASS
    @Bean
    @DependsOnDatabaseInitialization
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http
                // 1. Initialize authorization engine and OIDC structures cleanly
                .with(authorizationServerConfigurer, authorizationServer -> authorizationServer
                        .oidc(Customizer.withDefaults()))

                // 2. Define path authorization rules explicitly
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/debug/**", "/health", "/actuator/**",
                                "/swagger-ui/**")
                        .permitAll()
                        .anyRequest().authenticated())

                // 3. Register the interactive login portal in the exact same chain
                .formLogin(Customizer.withDefaults())

                // 4. Intercept unauthenticated entries smoothly and present the login form
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    @Bean
    @DependsOnDatabaseInitialization
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {
        JdbcRegisteredClientRepository repository = new JdbcRegisteredClientRepository(jdbcTemplate);

        // Natively bootstrap your API Gateway client if it's missing from the database
        if (repository.findByClientId("api-gateway-client") == null) {
            RegisteredClient gatewayClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("api-gateway-client")
                    // Stores the secret securely using your active BCrypt PasswordEncoder
                    .clientSecret(passwordEncoder.encode("123456"))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    // Use the exact gateway redirect URI matching your BFF application route
                    // patterns
                    .redirectUri("http://localhost:8080/login/oauth2/code/api-gateway-client")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope("offline_access")
                    .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(false).build())
                    .build();

            repository.save(gatewayClient);
        }

        return repository;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        // Satisfies the OIDC requirement by generating a decoder tied to your key pairs
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 2. PERSIST TOKENS: Stores active authorization flows, avoiding token dropouts
     * if server bounces.
     */
    @Bean
    @DependsOnDatabaseInitialization
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 3. PERSIST CONSENT: Tracks user access token permission grants permanently.
     */
    @Bean
    @DependsOnDatabaseInitialization
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * 4. PERSIST USERS: Swaps out InMemory engine for an active JDBC database
     * identity manager.
     */
    @Bean
    @DependsOnDatabaseInitialization
    public UserDetailsService userDetailsService(javax.sql.DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    // /**
    // * 5. ENFORCE SECURE HASHING: Leverages BCrypt automatically for production DB
    // * password strings.
    // */
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    // return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    // }

    /**
     * 5. ENFORCE SECURE HASHING: Forces BCrypt directly,
     * bypassing the need for prefixing database hash strings.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
