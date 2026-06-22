package com.bookstore.auth.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class AuthorizationServerConfig {

        @Value("${AUTH_SERVER_URL}")
        private String authServerIssuerUri;

        @Value("${GATEWAY_URL}")
        private String gatewayUrl;
        

        @Bean
        public JWKSource<SecurityContext> jwkSource(JwtKeyProperties jwtKeyProperties) throws Exception {
                RSAKey rsaKey = new RSAKey.Builder(jwtKeyProperties.getRsaPublicKey())
                                .privateKey(jwtKeyProperties.getRsaPrivateKey())
                                .keyID(UUID.randomUUID().toString())
                                .build();
                JWKSet jwkSet = new JWKSet(rsaKey);
                return new ImmutableJWKSet<>(jwkSet);
        }

        @Bean
        public AuthorizationServerSettings authorizationServerSettings() {
                return AuthorizationServerSettings.builder()
                                .issuer(this.authServerIssuerUri)
                                .authorizationEndpoint("/oauth2/authorize")
                                .tokenEndpoint("/oauth2/token")
                                .jwkSetEndpoint("/.well-known/jwks.json")
                                .build();
        }

}
